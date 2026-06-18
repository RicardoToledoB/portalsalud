package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.dto.RequestAttachmentDto;
import cl.dssm.soporteimagenes.entity.PortalImageRequest;
import cl.dssm.soporteimagenes.entity.RequestAttachment;
import cl.dssm.soporteimagenes.repository.RequestAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class RequestAttachmentService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final RequestAttachmentRepository repository;

    @Value("${app.upload.dir:uploads/solicitudes}")
    private String uploadDir;

    @Value("${app.upload.max-files:5}")
    private int maxFiles;

    @Value("${app.upload.max-file-size-bytes:5242880}")
    private long maxFileSizeBytes;

    @Transactional
    public List<RequestAttachment> saveAttachments(PortalImageRequest request, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<MultipartFile> validFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validFiles.isEmpty()) {
            return List.of();
        }
        if (validFiles.size() > maxFiles) {
            throw new ResponseStatusException(BAD_REQUEST, "Puede adjuntar máximo " + maxFiles + " imágenes por solicitud");
        }
        validFiles.forEach(this::validateFile);

        Path requestDir = baseDir().resolve(request.getFolio()).normalize();
        try {
            Files.createDirectories(requestDir);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No fue posible preparar el directorio de adjuntos");
        }

        return validFiles.stream().map(file -> saveOne(request, file, requestDir)).toList();
    }

    public RequestAttachmentDto toDto(RequestAttachment attachment) {
        return new RequestAttachmentDto(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                "/api/solicitudes/" + attachment.getRequest().getId() + "/adjuntos/" + attachment.getId() + "/download",
                attachment.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AttachmentFile download(Long requestId, Long attachmentId) {
        RequestAttachment attachment = repository.findByIdAndRequest_Id(attachmentId, requestId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Adjunto no encontrado"));
        Path path = baseDir().resolve(attachment.getRelativePath()).normalize();
        if (!path.startsWith(baseDir())) {
            throw new ResponseStatusException(BAD_REQUEST, "Ruta de adjunto inválida");
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(NOT_FOUND, "Archivo adjunto no disponible");
            }
            return new AttachmentFile(attachment.getOriginalFilename(), attachment.getContentType(), attachment.getSizeBytes(), resource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Archivo adjunto inválido");
        }
    }

    private RequestAttachment saveOne(PortalImageRequest request, MultipartFile file, Path requestDir) {
        validateFile(file);
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path target = requestDir.resolve(storedFilename).normalize();

        if (!target.startsWith(requestDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "Nombre de archivo inválido");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "No fue posible guardar el adjunto " + originalFilename);
        }

        Path relativePath = baseDir().relativize(target);
        RequestAttachment attachment = RequestAttachment.builder()
                .request(request)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .relativePath(relativePath.toString())
                .contentType(normalizeContentType(file.getContentType(), extension))
                .sizeBytes(file.getSize())
                .build();
        return repository.save(attachment);
    }

    private void validateFile(MultipartFile file) {
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        String contentType = normalizeContentType(file.getContentType(), extension);

        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WEBP");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new ResponseStatusException(BAD_REQUEST, "Cada imagen debe pesar máximo " + (maxFileSizeBytes / 1024 / 1024) + " MB");
        }
    }

    private Path baseDir() {
        return Path.of(uploadDir).toAbsolutePath().normalize();
    }

    private String sanitizeFilename(String filename) {
        String value = filename == null || filename.isBlank() ? "imagen" : filename.trim();
        value = value.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1);
        value = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        value = value.replaceAll("[^A-Za-z0-9._ -]", "_").replaceAll("\\s+", "_");
        return value.length() > 180 ? value.substring(value.length() - 180) : value;
    }

    private String extensionOf(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > -1 && idx + 1 < filename.length() ? filename.substring(idx + 1).toLowerCase() : "";
    }

    private String normalizeContentType(String contentType, String extension) {
        if (contentType != null && !contentType.isBlank()) {
            String lower = contentType.toLowerCase();
            if ("image/jpg".equals(lower)) {
                return "image/jpeg";
            }
            return lower;
        }
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    public record AttachmentFile(String originalFilename, String contentType, Long sizeBytes, Resource resource) {}
}
