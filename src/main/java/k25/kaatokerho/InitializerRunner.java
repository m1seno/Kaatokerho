package k25.kaatokerho;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import k25.kaatokerho.service.ExcelImportService;

//https://stackoverflow.com/questions/39280340/how-to-run-sql-scripts-and-get-data-on-application-startup
//https://www.baeldung.com/running-setup-logic-on-startup-in-spring
@Component
public class InitializerRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExcelImportService excelImportService;

    @Override
    public void run(String... args) throws Exception {
        // Tarkistetaan onko keilaaja-taulu tyhjä
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM keilaaja", Integer.class);

        if (count != null && count == 0) {
            System.out.println("Tietokanta tyhjä – ajetaan schema.sql ja data.sql");

            // Suoritetaan SQL-skriptit
            DataSource ds = jdbcTemplate.getDataSource();
            if (ds == null) {
                throw new IllegalStateException("DataSource ei ole asetettu!");
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                    new ClassPathResource("schema.sql"),
                    new ClassPathResource("data.sql")
            );
            populator.execute(ds);

            // Varmistetaan, että SQL-populointi onnistui (esim. keilaajia löytyi)
            Integer uusiCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM keilaaja", Integer.class);
            if (uusiCount == null || uusiCount == 0) {
                throw new IllegalStateException("Tietokanta jäi edelleen tyhjäksi schema.sql ja data.sql ajon jälkeen!");
            }

            System.out.println("Tuodaan tiedot Excelistä...");
            excelImportService.importExcel("src/main/resources/data.xlsx");

        } else {
            System.out.println("Tietokanta ei ole tyhjä – ei ajeta SQL-skriptejä eikä Excel-importtia");
        }
    }
}

