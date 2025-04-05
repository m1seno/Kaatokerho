package k25.kaatokerho;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import k25.kaatokerho.service.ExcelImportService;

@Component
public class ExcelImportRunner implements CommandLineRunner {

    private final ExcelImportService excelImportService;

    public ExcelImportRunner(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    @Override
    public void run(String... args) throws Exception {
        String tiedostoPolku = "src/main/resources/kaatokerhotilastot_2025-25.xlsx";
        if (excelImportService.isImportNeeded()) {
            System.out.println("Tietokanta tyhjä, tuodaan tiedot Excelistä...");
            try {
                excelImportService.importExcel(tiedostoPolku);
                System.out.println("Tietojen tuonti valmis.");
            } catch (Exception e) {
                System.err.println("Virhe Excel-tietojen tuonnissa: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Tietokanta ei ole tyhjä. Tuontia ei suoriteta.");
        }
    }
}
