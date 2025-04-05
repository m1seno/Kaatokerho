package k25.kaatokerho.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            if (kausiOptional.isEmpty())
                throw new IllegalArgumentException("Kautta 2024-2025 ei löytynyt");

            for (Row row : sheet) {
                // Skipataan otsikkorivi
                if (row.getRowNum() == 0) {
                    continue;
                }
                // Jos solu on tyhjä, lopetetaan silmukka
                if (row.getCell(1) == null) {
                    break;
                }

                // Tarkistetaan päivämäärä
                String[] pvmString = row.getCell(2).getStringCellValue().split("/");
                LocalDate pvm = LocalDate.of(
                        Integer.parseInt(pvmString[2]), // Vuosi
                        Integer.parseInt(pvmString[1]), // Kuukausi
                        Integer.parseInt(pvmString[0]) // Päivä
                );

                // Jos pvm vaihtuu JA ei olla ensimmäisellä kierroksella
                if (edellinenPvm != null && !pvm.equals(edellinenPvm)) {
                    // Käsitellään KuppiksenKunkku ja KeilaajaKausi
                    Optional<KuppiksenKunkku> edellinenOpt = kuppiksenKunkkuRepository
                            .findByGp_Jarjestysnumero(nykyinenGp.getJarjestysnumero());
                    nykyinenGp.setTulokset(nykyisetTulokset); // jotta palvelut saavat tulokset
                    kuppiksenKunkkuService.kasitteleKuppiksenKunkku(nykyinenGp, edellinenOpt.orElse(null));
                    keilaajaKausiService.paivitaKeilaajaKausi(nykyinenGp);

                    // Nollataan tilapäinen tuloslista
                    nykyisetTulokset.clear();
                }

                // GP haetaan tai luodaan
                if (nykyinenGp == null || !pvm.equals(edellinenPvm)) {
                    GP uusiGp = new GP();
                    uusiGp.setPvm(pvm);
                    uusiGp.setJarjestysnumero((int) row.getCell(1).getNumericCellValue());
                    uusiGp.setKausi(kausiOptional.get());

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
                boolean osallistui = (int) row.getCell(12).getNumericCellValue() == 1;
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

                // KultainenGP
                boolean onKultainenGp = ((int) row.getCell(23).getNumericCellValue()) == 1;
                kultainenGpService.kultainenPistelasku(onKultainenGp, sarja1, sarja2, keilaajaOptional.get(),
                        kausiOptional.get(), nykyinenGp);

                edellinenPvm = pvm;

            }

            // Viimeisen GP:n käsittely
            if (nykyinenGp != null && !nykyisetTulokset.isEmpty()) {
                nykyinenGp.setTulokset(nykyisetTulokset);
                Optional<KuppiksenKunkku> edellinenOpt = kuppiksenKunkkuRepository
                        .findByGp_Jarjestysnumero(nykyinenGp.getJarjestysnumero() - 1);
                kuppiksenKunkkuService.kasitteleKuppiksenKunkku(nykyinenGp, edellinenOpt.orElse(null));
                keilaajaKausiService.paivitaKeilaajaKausi(nykyinenGp);;
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}