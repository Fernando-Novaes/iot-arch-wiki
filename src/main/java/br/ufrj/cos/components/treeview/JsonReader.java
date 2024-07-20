package br.ufrj.cos.components.treeview;

import br.ufrj.cos.domain.IoTDomain;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonReader {
    public static List<IoTDomain> readJson(String filePath) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(filePath), new TypeReference<List<IoTDomain>>() {});
    }
}
