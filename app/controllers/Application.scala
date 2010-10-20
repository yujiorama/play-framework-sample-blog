package controllers

import play._
import play.cache.Cache
import play.mvc._
import play.data.validation._
import play.libs._

import models.Post

object Application extends Controller {
  
  val items_per_page = 10
  
  @Before
  def addDefaultRenderArgs {
    renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"))
    renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"))
  }
  
  def index {
    val frontposts = Post.find("order by postedAt desc").first
    val frontpost = frontposts match {
      case Some(post: Post) => post
      case None => None
    }
    val olderposts = Post.find("order by postedAt desc").from(1).fetch(items_per_page)

    render(frontpost, olderposts)
  }
  
  def show(id: Long) {
	val post = Post.findById(id) match {
      case Some(found: Post) => found
      case None => null
    }

    val randomId = Codec.UUID()
    render(post, randomId)

  }
  
  def postComment(
    id: Long,
    @Required (message="Author is required") author: String,
    @Required (message="A message is required") content: String,
    @Required (message="Please type the code") code: String,
    randomId: String
  ) {
    Post.findById(id) match {
      case Some(post: Post) => {
        Validation.current.equals(code, Cache.get(randomId).get)
        .message("Invalid code. Please type it again")
        if (Validation.hasErrors()) {
          renderTemplate("Application/show.html", post, randomId)
        }
        else {
          post.addComment(author, content)
          flash.success("Thanks for postings, %s", author)
        }
      }
      case None => None
    }

    show(id)
  }
  
  def captcha(id: String) {
    val captcha = Images.captcha()
    val code = captcha.getText("#E4EAFD")
    Cache.set(id, code, "10min")
    renderBinary(captcha)
  }
}
