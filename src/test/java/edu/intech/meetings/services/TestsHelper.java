package edu.intech.meetings.services;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.intech.meetings.model.Meeting;
import edu.intech.meetings.model.Room;
import edu.intech.meetings.model.User;

public class TestsHelper {

	static User JsonToUser(final String jsonString) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readerFor(User.class).readValue(jsonString);
	}

	static Meeting JsonToMeeting(final String jsonString) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readerFor(Meeting.class).readValue(jsonString);
	}

	static Room JsonToRoom(final String jsonString) throws IOException {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readerFor(Room.class).readValue(jsonString);
	}

	static List<Meeting> JsonToMeetingsList(final String jsonString) throws IOException {
		final TypeReference<List<Meeting>> mapType = new TypeReference<List<Meeting>>() {
		};
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, mapType);
	}
}
