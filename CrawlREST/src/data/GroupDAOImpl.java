package data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.RollbackException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Event;
import entities.Group;
import entities.Post;
import entities.Route;
import entities.RouteVenue;
import entities.User;
import entities.Venue;

@Transactional
@Repository
public class GroupDAOImpl implements GroupDAO {

	@PersistenceContext
	private EntityManager em;

	//Group Methods*****************************

	@Override
    public Group findGroupById(int gid) {
        Group g = em.find(Group.class, gid);
        return g;
    }

	@Override
	public Group createGroup(int uid, String groupJSON) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Group mappedGroup = mapper.readValue(groupJSON, Group.class);
			//Sets admin to the current user...cray cray
			User admin = em.find(User.class, uid);
			mappedGroup.setAdmin(admin);
			em.persist(mappedGroup);
			em.flush();
			if (mappedGroup.getUsers() == null) {
				mappedGroup.setUsers(new ArrayList<User>());
			}
			mappedGroup.getUsers().add(admin);
			return mappedGroup;

		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Group updateGroup(int gid, String groupJSON) {
		ObjectMapper mapper = new ObjectMapper();
		Group mappedGroup = null;
		try {

			mappedGroup = mapper.readValue(groupJSON, Group.class);

			Group g = em.find(Group.class, gid);
			g.setName(mappedGroup.getName());

			return g;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public Set<Group> findGroupByUserId(int uid) {
		String query = "SELECT u FROM User u WHERE u.id = :uid";
		List <User> users = em.createQuery(query, User.class).setParameter("uid", uid)
				.getResultList();
		return new HashSet<>(users.get(0).getGroups());
	}

	@Override
	public Boolean deleteGroup(int gid) {
		String query = "DELETE FROM Post p WHERE p.group.id = :gid";
		em.createQuery(query).setParameter("gid", gid).executeUpdate();
		
		Group group = em.find(Group.class, gid);
		if(group != null) {
			em.remove(group);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Group addUserToGroup(int uid, int gid) {
		Group g = null;
		User u = null;
		try {
			g = em.find(Group.class, gid);
			u = em.find(User.class, uid);
			g.getUsers().add(u);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return g;
	}

	@Override
	public Group removeUserFromGroup(int uid, int gid) {
		
		Group g = null;
		User u = null;
		try {
			g = em.find(Group.class, gid);
			u = em.find(User.class, uid);
			List<User> users = g.getUsers();
			
			//Setting to an outside cariable avoids concurrent modification exception
			//Cannot modify an arraylist while iterating through it
			User x = null;
			for (User user : users) {
				if (user.getId() == uid) {
					x = user;
				}
			}
			//Removal done post loop
			System.out.println(x);
			users.remove(x);
			//Set the new user list for that group
			g.setUsers(users);
		return g;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return g;
	}
//		String query = "DELETE FROM user_group ug WHERE ug.user.id = :uid AND ug.group.id = :gid";
//		int result = em.createQuery(query).executeUpdate();
//		if (result < 1) {
//			return false;
//		}
//		return true;
//	}


	//Event Methods*****************************

	@Override
	public Event createEvent(int uid, int gid, String eventJSON) {
		System.out.println("gid: " + gid);
		ObjectMapper mapper = new ObjectMapper();
		try {
			Event mappedEvent = mapper.readValue(eventJSON, Event.class);
			User user = em.find(User.class, uid);
			Group group = em.find(Group.class, gid);
			System.out.println("***********************************************");
			System.out.println(group);
			System.out.println(mappedEvent);
			mappedEvent.setAdmin(user);
			mappedEvent.setGroup(group);
			em.persist(mappedEvent);
			em.flush();
			return mappedEvent;

		} catch(RollbackException r) {
			r.printStackTrace();

		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Event findEventById(int eid) {
		Event event = em.find(Event.class, eid);
		System.out.println("***************************************************");
		System.out.println(event);
		return event;
	}

	@Override
	public Event updateEvent(int gid, int eid, String eventJSON) {
		ObjectMapper mapper = new ObjectMapper();
		Event mappedEvent = null;
		try {
			mappedEvent = mapper.readValue(eventJSON, Event.class);

			Event newEvent = em.find(Event.class, eid);
			if (newEvent.getGroup().getId() != gid) {
				return null;
			}
			newEvent.setName(mappedEvent.getName());
			newEvent.setDate(mappedEvent.getDate());

			//Update group and route?? **********************************

			return newEvent;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Boolean deleteEvent(int gid, int eid) {
		Event event = em.find(Event.class, eid);
		if(event != null && event.getGroup().getId() == gid) {
			em.remove(event);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Set<Event> findEventByGroupId(int gid) {
		String query = "SELECT e FROM Event e WHERE e.group.id = :gid";
		List <Event> events = em.createQuery(query, Event.class).setParameter("gid", gid)
				.getResultList();
		return new HashSet<>(events);
	}

	@Override
	public Set<Event> findEventsByUserId(int uid) {
		String query = "SELECT e FROM Event e JOIN e.group g JOIN g.users u JOIN u.groups WHERE u.id = :id";

		List<Event> eventList = em.createQuery(query, Event.class)
				.setParameter("id", uid)
				.getResultList();
		System.out.println(eventList);
		return new HashSet<>(eventList);

	}
	
	@Override
	public Event addRouteToEvent(int rid, int eid) {
		Route r = em.find(Route.class, rid);
		Event e = em.find(Event.class, eid);
		System.out.println("RID: " + rid);
		System.out.println("EID: " + eid);
		e.setRoute(r);
		
		return e;
	}

}
