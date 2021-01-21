package edu.intech.meetings.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the "Room" database table.
 *
 */
@Entity
@Table(name = "Room")
@NamedQueries({
        @NamedQuery(name = "Room.findById", query = "SELECT s FROM Room s WHERE s.id = :id"),
        @NamedQuery(name = "Room.findAll", query = "SELECT s FROM Room s"),
        @NamedQuery(name = "Room.findByMeeting", query = "SELECT m FROM Room m WHERE :id = ANY (SELECT u.id FROM m.meetings u)"),
        @NamedQuery(name = "Room.findByName", query = "SELECT s FROM Room s WHERE s.name = :name")
})
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private int capacity;
    private List<Meeting> meetings = new ArrayList<>();

    public Room() {
    }

    /**
     * @param name
     */
    public Room(String name) {
        this.name = name;
    }

    /**
     * @param name
     * @param capacity
     */
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }


    /**
     * @param name
     * @param capacity
     * @param meetings
     */
    public Room(String name, int capacity, List<Meeting> meetings) {
        this.name = name;
        this.capacity = capacity;
        this.meetings = meetings;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the capacity
     */
    @Column(name = "capacity", nullable = false)
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the associated room
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "Participants", joinColumns = @JoinColumn(name = "roomId"), inverseJoinColumns = @JoinColumn(name = "meetingId"))
    public List<Meeting> getMeetings() {
        return meetings;
    }

    /**
     * @param meetings
     */
    public void setMeetings(List<Meeting> meetings) {
        this.meetings = meetings;
    }
}
