package com.javarush.bookingsystem.service;


import com.javarush.bookingsystem.domain.Room;
import com.javarush.bookingsystem.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


@Service
public class RoomService {


    private final RoomRepository roomRepository;


    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }


    public Room addRoom(Room room) {
        if (room.getName() == null || room.getName().isBlank()) {
            throw new IllegalArgumentException("Название комнаты не указано");
        }
        if (room.getOpenTime() == null || room.getCloseTime() == null) {
            throw new IllegalArgumentException("Необходимо указать время открытия и закрытия комнаты");
        }
        if (!room.getOpenTime().isBefore(room.getCloseTime())) {
            throw new IllegalArgumentException("Время открытия должно быть раньше времени закрытия");
        }
        if (roomRepository.existsByNameIgnoreCase(room.getName())) {
            throw new IllegalArgumentException("Комната с таким названием уже существует");
        }
        return roomRepository.save(room);
    }


    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }
}