package edu.lmu.cs.wutup.ws.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.lmu.cs.wutup.ws.exception.EventExistsException;
import edu.lmu.cs.wutup.ws.exception.NoSuchEventException;
import edu.lmu.cs.wutup.ws.model.Event;

@Repository
public class EventDaoJdbcImpl implements EventDao {

    private static final String CREATE_SQL = "insert into event (id, name) values (?,?)";
    private static final String UPDATE_SQL = "update event set name=? where id=?";
    private static final String FIND_BY_ID_SQL = "select id, name from event where id=?";
    private static final String FIND_BY_NAME_SQL = "select id, name from event where name=? limit ? offset ?";
    private static final String FIND_ALL_SQL = "select id, name from event limit ? offset ?";
    private static final String DELETE_SQL = "delete from event where id=?";
    private static final String COUNT_SQL = "select count(*) from event";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void createEvent(Event e) {
        try {
            jdbcTemplate.update(CREATE_SQL, e.getId(), e.getName());
        } catch (DuplicateKeyException ex) {
            throw new EventExistsException();
        }
    }

    @Override
    public void updateEvent(Event e) {
        int rowsUpdated = jdbcTemplate.update(UPDATE_SQL, e.getName(), e.getId());
        if (rowsUpdated == 0) {
            throw new NoSuchEventException();
        }
    }

    @Override
    public Event findEventById(int id) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_SQL, new Object[]{id}, EventRowMapper);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new NoSuchEventException();
        }
    }

    @Override
    public List<Event> findEventsByName(String name, int pageNumber, int pageSize) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, new Object[]{name, pageSize, pageNumber * pageSize},
                EventRowMapper);
    }

    @Override
    public List<Event> findAllEvents(int pageNumber, int pageSize) {
        return jdbcTemplate.query(FIND_ALL_SQL, new Object[]{pageSize, pageNumber * pageSize}, EventRowMapper);
    }

    @Override
    public void deleteEvent(Event e) {
        int rowsUpdated = jdbcTemplate.update(DELETE_SQL, e.getId());
        if (rowsUpdated == 0) {
            throw new NoSuchEventException();
        }
    }

    @Override
    public int findNumberOfEvents() {
        return jdbcTemplate.queryForInt(COUNT_SQL);
    }

    private static RowMapper<Event> EventRowMapper = new RowMapper<Event>() {
        public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Event(rs.getInt("id"), rs.getString("name"));
        }
    };
}
