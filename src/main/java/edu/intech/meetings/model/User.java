package edu.intech.meetings.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the "User" database table.
 *
 */
@Entity
@Table(name = "User")
@NamedQueries({
		@NamedQuery(name = "User.findAll", query = "SELECT s FROM User s"),
		@NamedQuery(name = "User.findById", query = "SELECT s FROM User s WHERE s.id = :id"),
		@NamedQuery(name = "User.findByIdsList", query = "SELECT s FROM User s WHERE s.id in :ids"),
		@NamedQuery(name = "User.findByName", query = "SELECT s FROM User s WHERE s.name = :name")
})
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String firstName;
	private String eMail;
	private String password;

	public User() {
		this(null, null, null, null);
	}

	public User(final String name, final String password, final String firstName, final String eMail) {
		this.name = name;
		this.firstName = firstName;
		this.eMail = eMail;
		this.password = password;
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

	@Column(name = "name", nullable = false)
	public String getName() {
		return this.name;
	}

	public void setName(final String nom) {
		this.name = nom;
	}

	/**
	 * @return the firstName
	 */
	@Column(name = "firstName")
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the eMail
	 */
	@Column(name = "eMail")
	public String getEMail() {
		return this.eMail;
	}

	/**
	 * @param eMail the eMail to set
	 */
	public void setEMail(final String eMail) {
		this.eMail = eMail;
	}

	/**
	 * @return the password
	 */
	@Column(name = "password", nullable = false)
	public String getPassword() {
		return this.password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
	}

}