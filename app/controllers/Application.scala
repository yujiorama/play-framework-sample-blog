package controllers

import play._
import play.mvc._

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
	val post = Post.findById(id).get
	render(post)
  }
}
