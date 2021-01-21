package edu.intech.meetings.dao.interfaces;

import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.Room;

import java.util.List;

public interface IRoomDao {

    /**
     * @param room
     * @param useTransaction
     * @throws DaoException
     */
    void createRoom(Room room, boolean useTransaction) throws DaoException;

    /**
     * @param roomId
     * @return
     * @throws DaoException
     */
    Room readRoom(int roomId) throws DaoException;

    /**
     * @return
     * @throws DaoException
     */
    List<Room> readAllRooms() throws DaoException;

    /**
     * @param meetingId
     * @return
     * @throws DaoException
     */
    List<Room> readAllRoomsWithMeeting(int meetingId) throws DaoException;

    /**
     * @param room
     * @param useTransaction
     * @throws DaoException
     */
    void updateRoom(Room room, boolean useTransaction) throws DaoException;

    /**
     * @param room
     * @throws DaoException
     */
    void deleteRoom(Room room) throws DaoException;


}
