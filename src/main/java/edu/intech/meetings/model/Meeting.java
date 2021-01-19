package edu.intech.meetings.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * The persistent class for the "Meeting" database table.
 *
 */
@Entity
@Table(name = "Meeting")
@NamedQueries({
		@NamedQuery(name = "Meeting.findById", query = "SELECT s FROM Meeting s WHERE s.id = :id"),
		@NamedQuery(name = "Meeting.findAll", query = "SELECT s FROM Meeting s"),
		@NamedQuery(name = "Meeting.findByUser", query = "SELECT m FROM Meeting m WHERE :id = ANY (SELECT u.id FROM m.guests u)")
})
public class Meeting implements Serializable {

	public static final String DATETIME_PATTERN = "dd/MM/yyyy HH:mm";

	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private Date start;
	private int duration;
	private List<User> guests = new ArrayList<>();

	public Meeting() {
	}

	/**
	 * @param title
	 * @param start
	 * @param duration
	 * @param guests
	 */
	public Meeting(final String title, final Date start, final int duration) {
		this(title, start, duration, null);
	}

	/**
	 * @param title
	 * @param start
	 * @param duration
	 * @param guests
	 */
	public Meeting(final String title, final Date start, final int duration, final List<User> guests) {
		this.title = title;
		this.start = start;
		this.duration = duration;
		this.guests = guests;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	@Column(name = "title", nullable = false)
	public String getTitle() {
		return this.title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * @return the start date and time of the meeting
	 */
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm")
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start", nullable = false)
	public Date getStart() {
		return this.start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(final Date start) {
		this.start = start;
	}

	/**
	 * @return the duration of the meeting in minutes.
	 */
	@Column(name = "duration", nullable = false)
	public int getDuration() {
		return this.duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(final int duration) {
		this.duration = duration;
	}

	/**
	 * @return the guests
	 */
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinTable(name = "Guests", joinColumns = @JoinColumn(name = "meetingId"), inverseJoinColumns = @JoinColumn(name = "userId"))
	public List<User> getGuests() {
		return this.guests;
	}

	/**
	 * @param guests the guests to set
	 */
	public void setGuests(final List<User> guests) {
		this.guests = guests;
	}
}