package controllers

import play._
import play.mvc._
import play.data.validation._

import models.Post

object Application extends Controller {
  
  val items_per_page = 10
  
  @Before
  def addDefaultRenderArgs() {
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
  
  // def show(id: Long) {
  //   val found = Post.findById(id)
  //   found match {
  //     case Some(post: Post) => render(post)
  //     case None => None
  //   }
  // }

  def show(id: Long) {
	val post = Post.findById(id) match {
      case Some(found: Post) => found
      case None => null
    }
    render(post)
  }
  
  def postComment(id: Long, @Required author: String, @Required content: String) {
    Post.findById(id) match {
      case Some(post: Post) => {
        if (Validation.hasErrors()) {
          renderTemplate("Application/show.html", post)
        }
        post.addComment(author, content)
        flash.success("Thanks for postings, %s", author)
      }
      case None => None
    }

    show(id)
  }
}
