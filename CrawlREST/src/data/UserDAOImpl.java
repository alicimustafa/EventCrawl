package data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import entities.Contact;
import entities.Event;
import entities.Group;
import entities.Login;
import entities.Post;
import entities.Route;
import entities.User;

@Transactional
@Repository
public class UserDAOImpl implements UserDAO {

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ContactDAO contactDao;

	@Autowired
	private PasswordEncoder encoder;

	@Override
	public Set<User> indexUsers() {
		String query = "SELECT u FROM User u";
		return new HashSet<User>(em.createQuery(query, User.class).getResultList());
	}


	// Login (User)
	@Override //works
	public Login loginUser(String crawlJson) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Login mappedLogin = mapper.readValue(crawlJson, Login.class);
			String query = "SELECT l FROM Login l WHERE l.username = :username";
			List<Login> login = em.createQuery(query, Login.class)
			          .setParameter("username", mappedLogin.getUsername())
			          .getResultList();
			if (login.size() > 0) {
				System.out.println("correct size");
			   boolean passwordsDoMatch = encoder.matches(mappedLogin.getPassword(), login.get(0).getPassword());
			   if(passwordsDoMatch) {
				   return login.get(0);
			   }
			}
			  
		}catch (Exception e) {
			System.out.println("execption");
			e.printStackTrace();
		}
		   
	return null;
	}
	

	@Override // works
	public Login registerUser(String crawlJson) {
		ObjectMapper mapper = new ObjectMapper();
		User user = new User();
		Contact contact = new Contact();
		em.persist(contact);
		em.flush();
		user.setContact(contact);
		em.persist(user);
		em.flush();
		try {
			Login mappedLogin = mapper.readValue(crawlJson, Login.class);
			mappedLogin.setUser(user);
			String password = encoder.encode(mappedLogin.getPassword());
			mappedLogin.setPassword(password);
			em.persist(mappedLogin);
			em.flush();

			return mappedLogin;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// User
	@Override
	public User findUser(int id) {
		User u = em.find(User.class, id);
		System.out.println(u.getGroups().size());
		return u;
	}

	@Override // works
	public Set<User> indexUserByGroup(int gid) {
		String query = "SELECT g FROM Group g where g.id = :id"; // JPQL
		List<Group> groups = em.createQuery(query, Group.class).setParameter("id", gid).getResultList();
		if (groups.size() < 1) {
			return null;
		} else {
			return new HashSet<User>(groups.get(0).getUsers());
		}
	}

	@Override //works
	public User updateUser(int id, String crawlJson) {
		ObjectMapper mapper = new ObjectMapper();
		User mappedUser = null;
		try {
			mappedUser = mapper.readValue(crawlJson, User.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		User u = em.find(User.class, id);
		if(mappedUser.getContact() != null) {
			Contact c = contactDao.updateContact(id, mappedUser.getContact().getId(), mappedUser.getContact());
			u.setContact(c);
		}
		u.setFirstName(mappedUser.getFirstName());
		u.setLastName(mappedUser.getLastName());
		u.setImgUrl(mappedUser.getImgUrl());
		return u;
	}


	@Override //works
	public User addContactToUser(Contact contact, int id) {
		User user = em.find(User.class, id);
		if (user != null) {
			user.setContact(contact);
			return user;
		}
		return null;
	}

	// Posts
	@Override // works
	public Set<Post> findPostByUser(int id) {
		String query = "SELECT p FROM Post p where p.user.id = :id"; // JPQL
		List<Post> p = em.createQuery(query, Post.class).setParameter("id", id).getResultList();
		return new HashSet<Post>(p);

	}

	@Override // works
	public Set<Post> findPostByGroup(int gid) {
		String query = "SELECT p FROM Post p where p.group.id = :id"; // JPQL
		List<Post> p = em.createQuery(query, Post.class).setParameter("id", gid).getResultList();
		return new HashSet<Post>(p);

	}

	@Override // works
	public Post createPost(int id, int gid, String crawlJson) {
		 ObjectMapper mapper = new ObjectMapper();
		 
		
		 try {
		 Post mappedPost = mapper.readValue(crawlJson, Post.class);
		 User u = em.find(User.class, id); //mapping the foreign key and associates
		 Group g = em.find(Group.class, gid); 
		 mappedPost.setUser(u);
		 mappedPost.setGroup(g);
		 em.persist(mappedPost);
		 em.flush();
		
		 return mappedPost;
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		return null;
	}

	@Override // not tested
	public Post updatePost(int pid, String crawlJson) {
		ObjectMapper mapper = new ObjectMapper();
		Post mappedPost = null;
		try {
			mappedPost = mapper.readValue(crawlJson, Post.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Post p = em.find(Post.class, pid);
		p.setMessage(mappedPost.getMessage());

		return p;
	}

	@Override // not tested
	public Boolean deletePost(int pid) {
		Post p = em.find(Post.class, pid);
		try {
			em.remove(p);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public User groupAdminCheck(int uid, int gid) {
		Group group = em.find(Group.class, gid);
		User user = em.find(User.class, uid);
		if(group.getAdmin()==user) {
		return user ;
	}
		return null;
}
	
	@Override
	public User eventAdminCheck(int uid, int eid) {
		Event event = em.find(Event.class, eid);
		User user = em.find(User.class, uid);
		if(event.getAdmin()==user) {
		return user ;
	}
		return null;
}
	@Override
	public User routeAdminCheck(int uid, int rid) {
		Route route = em.find(Route.class, rid);
		User user = em.find(User.class, uid);
		if(route.getAdmin()==user) {
		return user ;
	}
		return null;
}

}
