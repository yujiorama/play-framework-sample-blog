import org.junit._
import org.junit.Assert._

import play.test._

import models._

class BasicTest extends UnitTest {
  @Before
  def setup() {
    Fixtures.deleteAll()
  }

  @Test
  def createAndRetrieveUser() {
    new User("bob@example.com", "secret", "Bob").save()
    
    val bob = User.find("byEmail", "bob@example.com").first
    
    assertNotNull(bob)
    assertEquals("Bob", bob.get.fullname)
  }

  @Test
  def tryConnectAsUser() {
    new User("bob@example.com", "secret", "Bob").save()
    
    assertNotNull(User.connect("bob@example.com", "secret"))
    assertEquals(None, User.connect("bob@example.com", "badpassword"))
    assertEquals(None, User.connect("tommy@example.com", "secret"))
  }
  
  @Test
  def createPost() {
    val bob = new User("bob@example.com", "secret", "Bob").save()
    new Post(bob, "first post", "hello world").save()
    assertEquals(1L, Post.count())
    
    val bobspost = Post.find("byAuthor", bob).fetch
    assertEquals(1, bobspost.length)
    val firstPost = bobspost.head
    assertNotNull(firstPost)
    assertEquals(bob, firstPost.author)
    assertEquals("first post", firstPost.title)
    assertEquals("hello world", firstPost.content)
    assertNotNull(firstPost.postedAt)
  }
  
  @Test
  def postComment() {
    val bob = new User("bob@example.com", "secret", "Bob").save()
    val bobspost = new Post(bob, "first post", "hello world").save()
    
    new Comment(bobspost, "Jeff", "nice post").save()
    new Comment(bobspost, "Tom", "i knew that !").save()
    
    val bobspostcomments = Comment.find("byPost", bobspost).fetch
    
    assertEquals(2, bobspostcomments.length)
    val firstcomment = bobspostcomments(0)
    assertEquals("Jeff", firstcomment.author)
    assertEquals("nice post", firstcomment.content)
    assertNotNull(firstcomment.postedAt)
    
    val secondcomment = bobspostcomments(1)
    assertNotNull(secondcomment)
    assertEquals("Tom", secondcomment.author)
    assertEquals("i knew that !", secondcomment.content)
    assertNotNull(secondcomment.postedAt)
  }
  
  @Test
  def useTheCommentsRelation() {
    val bob = new User("bob@example.com", "secret", "Bob").save()
    val bobspost = new Post(bob, "first post", "hello world").save()
    
    bobspost.addComment("Jeff", "nice post")
    bobspost.addComment("Tom", "i knew that !")
    
    assertEquals(1L, User.count())
    assertEquals(1L, Post.count())
    assertEquals(2L, Comment.count())
    
    val bobposts = Post.find("byAuthor", bob).first
    bobposts match {
      case Some(posts: Post) => {
        assertEquals(2, posts.comments.size)
        assertEquals("Jeff", posts.comments.get(0).author)
        posts.delete()
      }
      case None => fail("Post.findByAuthor return None")
    }
    
    assertEquals(1L, User.count())
    assertEquals(0L, Post.count())
    assertEquals(0L, Comment.count())
  }
  
  @Test
  def fullTest() {
    Fixtures.load("data.yml")
    
    assertEquals(2L, User.count())
    assertEquals(3L, Post.count())
    assertEquals(3L, Comment.count())
    
    assertNotNull(User.connect("bob@example.com", "secret"))
    assertNotNull(User.connect("jeff@example.com", "secret"))
    assertEquals(None, User.connect("jeff@example.com", "badpassword"))
    assertEquals(None, User.connect("tom@example.com", "secret"))
    
    val bobposts = Post.find("author.email", "bob@example.com").fetch
    assertEquals(2, bobposts.length)
    
    val bobcomments = Comment.find("post.author.email", "bob@example.com").fetch
    assertEquals(3, bobcomments.length)
    
    val frontpost = Post.find("order by postedAt desc").first
    frontpost match {
      case Some(posts: Post) => {
        assertEquals("About the model layer", posts.title)
        assertEquals(2, posts.comments.size)
        posts.addComment("Jim", "Hello guys")
        assertEquals(3, posts.comments.size)
        assertEquals(4L, Comment.count())
      }
      case None => fail("find the most recent posts")
    }
  }
  
  @Test
  def postPaginationTest() {
    Fixtures.load("data.yml")
    
    val firstpost = Post.find("order by postedAt desc").first
    firstpost match {
      case Some(first: Post) => {
        val second = first.next()
        assertNotNull(second)
        assertEquals(true, first.postedAt.compareTo(second.postedAt) > 0)
        val third = second.next()
        assertNotNull(third)
        assertEquals(true, second.postedAt.compareTo(third.postedAt) > 0)
        assertEquals(true, first.postedAt.compareTo(third.postedAt) > 0)
        val second2 = third.previous()
        assertNotNull(second2)
        assertEquals(second, second2)
        val first2 = second.previous()
        assertNotNull(first2)
        assertEquals(first, first2)
        
        assertNull(first.previous())
        assertNull(third.next())
      }
      case None => fail("empty data")
    }
  }

  @Test
  def testTag() {
    val tag = Tag.findOrCreateByName("A")
    assertNotNull(tag)
    assertEquals(1, Tag.count)
    Tag.findOrCreateByName("B")
    assertEquals(2, Tag.count)
  }

  @Test
  def testPostWithTags() {
    val bob = new User("bob@example.com", "secret", "Bob").save()
    val bobspost1 = new Post(bob, "first post", "hello world").save()
    val bobspost2 = new Post(bob, "Hop", "hello world").save()

    assertEquals(0, Post.findTaggedWith("Red").size)
    
    bobspost1.tagItWith("Red").tagItWith("Blue").save()
    bobspost2.tagItWith("Red").tagItWith("Green").save()
    
    assertEquals(2, Post.findTaggedWith("Red").size)
    assertEquals(1, Post.findTaggedWith("Blue").size)
    assertEquals(1, Post.findTaggedWith("Green").size)
    
    assertEquals(1, Post.findTaggedWith("Red", "Blue").size)
    assertEquals(1, Post.findTaggedWith("Red", "Green").size)
    assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size)
    assertEquals(0, Post.findTaggedWith("Green", "Blue").size)
    
    val cloud = Tag.getCloud
    assertNotNull(cloud)
    assertEquals("List(Map(tag -> Blue, pound -> 1), Map(tag -> Green, pound -> 1), Map(tag -> Red, pound -> 2))", cloud.toString)
  }
}
