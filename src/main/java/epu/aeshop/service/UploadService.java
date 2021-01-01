package epu.aeshop.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.multipart.MultipartFile;
import epu.aeshop.ShoppingApplication;

public class UploadService {

    private final Path root;

    public UploadService(String root) {
        this.root = new ApplicationHome(ShoppingApplication.class).getDir().toPath().resolve(root);
    }

    public void save(MultipartFile multipart, String filename) throws IOException {
        Path target = Paths.get(this.root.toString(), filename);
        Path targetDir = target.getParent();

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Files.copy(multipart.getInputStream(), target);
    }
}
