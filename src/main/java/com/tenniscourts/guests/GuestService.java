package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public List<GuestDTO> findAll() {
        return guestMapper.map(guestRepository.findAll());
    }

    public GuestDTO findById(Long guestId) {
        return guestRepository.findById(guestId)
                .map(guestMapper::map)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException(String.format("Guest with id %s was not found", guestId));
                });
    }

    public GuestDTO findByName(String name) {
        return guestRepository.findByName(name)
                .map(guestMapper::map)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException(String.format("Guest with name %s was not found", name));
                });
    }

    public GuestDTO add(GuestDTO guest) {
        return guestMapper.map(guestRepository.save(guestMapper.map(guest)));
    }

    public GuestDTO update(GuestDTO guestDTO) {
        findById(guestDTO.getId());
        return guestMapper.map(guestRepository.save(guestMapper.map(guestDTO)));
    }

    public void delete(Long guestId) {
        findById(guestId);
        guestRepository.deleteById(guestId);
    }

}
