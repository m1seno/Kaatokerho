package k25.kaatokerho.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;

//https://www.youtube.com/watch?v=ipjl49Hgsg8&list=PLUDwpEzHYYLsN1kpIjOyYW6j_GLgOyA07&index=1
@Service
public class ExcelImportService {

    @Autowired
    private TulosRepository tulosRepository;

    @Autowired
    private GpRepository gpRepository;

    @Autowired
    private KeilahalliRepository keilahalliRepository;

    @Autowired
    private KeilaajaRepository keilaajaRepository;

    @Autowired
    private KausiRepository kausiRepository;

    @Autowired
    private KultainenGpService kultainenGpService;

    @Autowired
    private KuppiksenKunkkuService kuppiksenKunkkuService;

    @Autowired
    private KuppiksenKunkkuRepository kuppiksenKunkkuRepository;

    @Autowired
    private KeilaajaKausiService keilaajaKausiService;

    // Luetaan tiedot excelistä vain, jos kyseiset taulut ovat tyhjiä
    public boolean isImportNeeded() {
        return (tulosRepository.count() == 0 && gpRepository.count() == 0);
    }

    public void importExcel(String filePath) throws IOException {
        // Luetaan Excel-tiedoston data-välilehti inputstreamistä
        try (FileInputStream inputStream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheet("Data");

            // For -loopin ulkopuolella säilytettäviä tietoja
            GP nykyinenGp = null;
            LocalDate edellinenPvm = null;
            List<Tulos> nykyisetTulokset = new ArrayList<>();
            Optional<Kausi> kausiOptional = kausiRepository.findByNimi("2024-2025");
            Row viimeisinRivi = null;
            if (kausiOptional.isEmpty())
                throw new IllegalArgumentException("Kautta 2024-2025 ei löytynyt");

            for (Row row : sheet) {

                // Debug-printti rivistä
                System.out.println("Käsitellään rivi: " + (row.getRowNum() + 1));

                // Skipataan otsikkorivi
                if (row.getRowNum() == 0) {
                    continue;
                }
                if (row.getRowNum() == 181) {
                    System.out.println("Lopetetaan excelin rivillä 182");
                    break;
                }

                // ChatGPT:n tekemää debuggausta
                // Tarkistetaan päivämäärä
                Cell cell = row.getCell(2);
                LocalDate pvm;

                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    throw new IllegalArgumentException("Päivämäärän solu puuttuu riviltä " + (row.getRowNum() + 1));
                }

                switch (cell.getCellType()) {
                    case NUMERIC -> {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            pvm = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        } else {
                            throw new IllegalArgumentException(
                                    "NUMERIC-muotoinen solu ei ole päivämäärä rivillä " + (row.getRowNum() + 1));
                        }
                    }

                    case STRING -> {
                        try {
                            String[] pvmString = cell.getStringCellValue().split("/");
                            pvm = LocalDate.of(
                                    Integer.parseInt(pvmString[2]),
                                    Integer.parseInt(pvmString[1]),
                                    Integer.parseInt(pvmString[0]));
                        } catch (Exception e) {
                            throw new IllegalArgumentException(
                                    "Päivämäärän tekstimuoto on virheellinen rivillä " + (row.getRowNum() + 1));
                        }
                    }

                    case FORMULA -> {
                        CellType resultType = cell.getCachedFormulaResultType();
                        if (resultType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            pvm = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        } else if (resultType == CellType.STRING) {
                            try {
                                String[] pvmString = cell.getStringCellValue().split("/");
                                pvm = LocalDate.of(
                                        Integer.parseInt(pvmString[2]),
                                        Integer.parseInt(pvmString[1]),
                                        Integer.parseInt(pvmString[0]));
                            } catch (Exception e) {
                                throw new IllegalArgumentException(
                                        "Kaavan palauttama päivämäärä (STRING) on virheellinen rivillä "
                                                + (row.getRowNum() + 1));
                            }
                        } else {
                            throw new IllegalArgumentException(
                                    "Kaavan palauttama arvo ei ole tuettu rivillä " + (row.getRowNum() + 1));
                        }
                    }

                    default -> throw new IllegalArgumentException(
                            "Päivämäärän solun tyyppi ei ole tuettu rivillä " + (row.getRowNum() + 1) +
                                    ". Tyyppi: " + cell.getCellType());
                }

                // Jos pvm vaihtuu JA ei olla ensimmäisellä kierroksella
                if (edellinenPvm != null && !pvm.equals(edellinenPvm)) {

                    nykyinenGp.setTulokset(nykyisetTulokset); // jotta palvelut saavat tulokset

                    // Kultaiset GP-pisteet laskumetodia kutsutaan

                    kultainenGpService.kultainenPistelasku(nykyinenGp);

                    // Haetaan tieto onko vyö unohtunut (1 = kyllä, 0 = ei)
                    boolean vyoUnohtui = false;
                    Cell vyoCell = row.getCell(67);
                    if (vyoCell != null && vyoCell.getCellType() == CellType.NUMERIC) {
                        vyoUnohtui = (int) vyoCell.getNumericCellValue() == 1;
                    }

                    // Käsitellään KuppiksenKunkku ja KeilaajaKausi
                    Optional<KuppiksenKunkku> edellinenOpt = kuppiksenKunkkuRepository
                            .findByGp_Jarjestysnumero(nykyinenGp.getJarjestysnumero());
                    kuppiksenKunkkuService.kasitteleKuppiksenKunkku(nykyinenGp, edellinenOpt.orElse(null), vyoUnohtui);
                    keilaajaKausiService.paivitaKeilaajaKausi(nykyinenGp);

                    // Nollataan tilapäinen tuloslista
                    nykyisetTulokset.clear();
                }

                // GP haetaan tai luodaan
                if (nykyinenGp == null || !pvm.equals(edellinenPvm)) {
                    edellinenPvm = pvm;

                    GP uusiGp = new GP();
                    uusiGp.setPvm(pvm);
                    uusiGp.setJarjestysnumero((int) row.getCell(1).getNumericCellValue());
                    uusiGp.setKausi(kausiOptional.get());
                    boolean onKultainenGp = ((int) row.getCell(23).getNumericCellValue()) == 1;
                    uusiGp.setOnKultainenGp(onKultainenGp);

                    String[] keilahalli = row.getCell(3).getStringCellValue().split(" ");
                    String hakusana = keilahalli[0];
                    Optional<Keilahalli> keilahalliOptional = keilahalliRepository
                            .findByNimiContainingIgnoreCase(hakusana);
                    if (keilahalliOptional.isEmpty())
                        throw new IllegalArgumentException("Keilahallia ei löytynyt hakusanalla: " + hakusana);
                    uusiGp.setKeilahalli(keilahalliOptional.get());

                    nykyinenGp = gpRepository.save(uusiGp);
                }

                // Haetaan keilaaja
                String[] nimi = row.getCell(8).getStringCellValue().split(" ");
                Optional<Keilaaja> keilaajaOptional = keilaajaRepository.findByEtunimiAndSukunimi(nimi[0], nimi[1]);

                // Luodaan tulos
                boolean osallistui = ((int) row.getCell(12).getNumericCellValue()) == 1;
                Integer sarja1 = osallistui && row.getCell(9) != null ? (int) row.getCell(9).getNumericCellValue()
                        : null;
                Integer sarja2 = osallistui && row.getCell(10) != null ? (int) row.getCell(10).getNumericCellValue()
                        : null;

                Tulos tulos = new Tulos();
                tulos.setSarja1(sarja1);
                tulos.setSarja2(sarja2);
                tulos.setOsallistui(osallistui);
                tulos.setKeilaaja(keilaajaOptional.get());
                tulos.setGp(nykyinenGp);
                tulosRepository.save(tulos);

                nykyisetTulokset.add(tulos);

                viimeisinRivi = row;

            }

            // Viimeisen GP:n käsittely
            if (nykyinenGp != null && !nykyisetTulokset.isEmpty()) {
                nykyinenGp.setTulokset(nykyisetTulokset);

                kultainenGpService.kultainenPistelasku(nykyinenGp);

                boolean vyoUnohtui = false;
                if (viimeisinRivi != null) {
                    Cell vyoCell = viimeisinRivi.getCell(67);
                    if (vyoCell != null && vyoCell.getCellType() == CellType.NUMERIC) {
                        vyoUnohtui = (int) vyoCell.getNumericCellValue() == 1;
                    }
                }

                Optional<KuppiksenKunkku> edellinenOpt = kuppiksenKunkkuRepository
                        .findByGp_Jarjestysnumero(nykyinenGp.getJarjestysnumero() - 1);
                kuppiksenKunkkuService.kasitteleKuppiksenKunkku(nykyinenGp, edellinenOpt.orElse(null), vyoUnohtui);
                keilaajaKausiService.paivitaKeilaajaKausi(nykyinenGp);

            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}