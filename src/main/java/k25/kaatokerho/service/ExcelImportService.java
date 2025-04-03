package k25.kaatokerho.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.TulosRepository;

//https://www.youtube.com/watch?v=ipjl49Hgsg8&list=PLUDwpEzHYYLsN1kpIjOyYW6j_GLgOyA07&index=1
@Service
public class ExcelImportService {

    @Autowired
    private TulosRepository tulosRepository;

    public boolean isImportNeeded() {
        return tulosRepository.count() == 0;
    }

    public void importExcel(String filePath) {

    }
}
