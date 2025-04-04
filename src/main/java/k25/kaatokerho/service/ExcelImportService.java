package k25.kaatokerho.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
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
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.KultainenGp;
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
    private KeilaajaKausi keilaajaKausi;

    @Autowired
    private KultainenGp kultainenGp;

    @Autowired
    private KuppiksenKunkku kuppiksenKunkku;

    @Autowired
    private KultainenGpService kultainenGpService;

    @Autowired
    private KuppiksenKunkkuService kuppiksenKunkkuService;

    @Autowired
    private KuppiksenKunkkuRepository kuppiksenKunkkuRepository;

    // Luetaan tiedot excelistä vain, jos tietokanta on tyhjä
    public boolean isImportNeeded() {
        return (tulosRepository.count() == 0 && gpRepository.count() == 0);
    }

    public void importExcel(String filePath) throws IOException {
        // Luetaan Excel-tiedoston data-välilehti inputstreamistä
        try (FileInputStream inputStream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheet("Data");

            for (Row row : sheet) {
                // Skipataan otsikkorivi
                if (row.getRowNum() == 0) {
                    continue;
                }
                // Jos solu on tyhjä, lopetetaan silmukka
                if (row.getCell(1) == null) {
                    break;
                }

                GP gp = new GP();
                Tulos tulos = new Tulos();

                // Haetaan keilaajan tiedot riviltä
                String[] nimi = row.getCell(8).getStringCellValue().split(" ");
                String etunimi = nimi[0];
                String sukunimi = nimi[1];
                // Haetaan keilaaja -olio tietokannasta
                Optional<Keilaaja> keilaajaOptional = keilaajaRepository.findByEtunimiAndSukunimi(etunimi, sukunimi);

                // Keilahallin tiedot
                String[] keilahalli = row.getCell(3).getStringCellValue().split(" ");
                String hakusana = keilahalli[0];
                Optional<Keilahalli> keilahalliOptional = keilahalliRepository.findByNimiContainingIgnoreCase(hakusana);

                // Kauden tiedot
                Optional<Kausi> kausiOptional = kausiRepository.findByNimi("2024-2025");

                // GP:n tiedot
                String[] pvmString = row.getCell(2).getStringCellValue().split("/");
                LocalDate pvm = LocalDate.of(
                        Integer.parseInt(pvmString[2]), // Vuosi
                        Integer.parseInt(pvmString[1]), // Kuukausi
                        Integer.parseInt(pvmString[0]) // Päivä
                );
                Optional<GP> gpOpt = gpRepository.findByPvm(pvm);

                // Jos Gp:tä ei löydy, luodaan GP-olio, asetetaan sen tiedot ja tallennetaan
                // tietokantaan
                if (!gpOpt.isPresent()) {
                    Integer jarjestysnumero = (int) row.getCell(1).getNumericCellValue();

                    gp.setKausi(kausiOptional.get());
                    gp.setKeilahalli(keilahalliOptional.get());
                    gp.setPvm(pvm);
                    gp.setJarjestysnumero(jarjestysnumero);

                    gpRepository.save(gp);
                }

                // Tuloksen tiedot
                Integer sarja1 = (int) row.getCell(9).getNumericCellValue();
                Integer sarja2 = (int) row.getCell(10).getNumericCellValue();
                Integer osallistuiInt = (int) row.getCell(12).getNumericCellValue();
                boolean osallistui = false;

                if (osallistuiInt == 1) {
                    osallistui = true;
                }

                tulos.setSarja1(sarja1);
                tulos.setSarja2(sarja2);
                tulos.setOsallistui(osallistui);
                tulos.setKeilaaja(keilaajaOptional.get());
                tulos.setGp(gp);

                // KultaisenGP:n tiedot
                Integer kultainenString = (int) row.getCell(23).getNumericCellValue();
                boolean onKultainenGp = false;
                if (kultainenString == 1) {
                    onKultainenGp = true;
                }
                kultainenGpService.kultainenPistelasku(onKultainenGp, sarja1, sarja2, keilaajaOptional.get(),
                        kausiOptional.get(), gp);

                // KuppiksenKunkun tiedot
                int edellinenJarjestys = gp.getJarjestysnumero() - 1;
                Optional<KuppiksenKunkku> edellinenOpt = kuppiksenKunkkuRepository.findByGp_Jarjestysnumero(edellinenJarjestys);

                kuppiksenKunkkuService.kasitteleKuppiksenKunkku(gp, edellinenOpt.orElse(null));
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}