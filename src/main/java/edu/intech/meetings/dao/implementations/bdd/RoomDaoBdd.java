package edu.intech.meetings.dao.implementations.bdd;

import edu.intech.meetings.dao.DaoFactory;
import edu.intech.meetings.dao.interfaces.IRoomDao;
import edu.intech.meetings.exceptions.DaoException;
import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.Room;
import edu.intech.meetings.utils.DaoHelper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

public class RoomDaoBdd implements IRoomDao {
    
    private final EntityManager em;

    /**
     * @param factory 
     */
    public RoomDaoBdd(final EntityManager em) {
        this.em = em;
    }

    @Override
    public void createRoom(Room room, boolean useTransaction) throws DaoException {
        try {
            DaoHelper.persistObject(room, this.em, useTransaction);
        } catch (final PersistenceException e) {
            throw new DaoException("Impossible de créer la réunion", e);
        }
    }

    @Override
    public Room readRoom(int roomId) throws DaoException {
        final TypedQuery<Room> query = this.em.createNamedQuery("Room.findById", Room.class);
        query.setParameter("id", roomId);
        final List<Room> ret = query.getResultList();
        if (ret.size() > 0) {
            return ret.get(0);
        }
        return null;    }

    @Override
    public List<Room> readAllRooms() throws DaoException {
        final TypedQuery<Room> query = this.em.createNamedQuery("Room.findAll", Room.class);
        return query.getResultList();
    }

    @Override
    public List<Room> readAllRoomsWithMeeting(int meetingId) throws DaoException {
        final TypedQuery<Room> query = this.em.createNamedQuery("Room.findByMeeting", Room.class);
        query.setParameter("id", meetingId);
        return query.getResultList();    }

    @Override
    public void updateRoom(Room room, boolean useTransaction) throws DaoException {
        try {
            DaoHelper.mergeObject(room, this.em, useTransaction);
        } catch (final PersistenceException e) {
            throw new DaoException("Impossible de modifier la salle", e);
        }
    }

    @Override
    public void deleteRoom(Room room) throws DaoException {
        try {
            // Pour éviter une violation de contrainte, on enlève les invités éventuels de
            // la réunion avant de l'effacer.
            room.setMeetings(new ArrayList<Meeting>());
            DaoFactory.getInstance().openTransaction();
            DaoHelper.mergeObject(room, this.em, false);
            DaoHelper.removeObject(room, this.em, false);
            DaoFactory.getInstance().commitTransaction();
        } catch (final PersistenceException e) {
            DaoFactory.getInstance().rollbackTransaction();
            throw new DaoException("Impossible de supprimer la salle", e);
        }
    }
}
