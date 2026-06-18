package cl.dssm.soporteimagenes.service;

import cl.dssm.soporteimagenes.repository.PortalImageRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FolioService {
    private final PortalImageRequestRepository repository;

    public synchronized String nextFolio() {
        int year = LocalDate.now().getYear();
        long next = repository.count() + 1;
        String folio;
        do {
            folio = String.format("REQ-%d-%06d", year, next++);
        } while (repository.existsByFolio(folio));
        return folio;
    }
}
