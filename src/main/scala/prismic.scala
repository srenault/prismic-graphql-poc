package prismic

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future
import io.prismic.fragments._

object data {

  class Repository {

    def getBlogPost(uid: String): Option[BlogPost] = Some(
      BlogPost(
        "blogpost1",
        StructuredText(Seq(StructuredText.Block.Heading("Blog post #1", Seq.empty, 1, None, None))),
        StructuredText(Seq(StructuredText.Block.Paragraph("This blog post is about GraphQL with prismic.io", Seq.empty, None, None))),
        Image(
          Image.View("http://image1", 100, 100, Some("alt")),
          Map.empty
        )
      )
    )
  }

  case class BlogPost(
    uid: String,
    title: StructuredText,
    shortlede: StructuredText,
    image: Image
  )
}

object schema {

  val blogposts = Fetcher.caching {
    (ctx: data.Repository, uids: Seq[String]) => {
      Future.successful(uids.flatMap(uid ⇒ ctx.getBlogPost(uid)))
    }
  }(HasId(_.uid))

  val BlogPost =
    ObjectType(
      "BlogPost",
      "An blog post",
      () => fields[data.Repository, data.BlogPost](
        Field("uid", StringType,
          Some("The uid of the blog post."),
          resolve = _.value.uid
        )
      )
    )

  val UID = Argument("uid", StringType, description = "uid of the blog post")

  val UID2 = Argument("uid2", StringType, description = "uid2 of the blog post")

  val Query =
    ObjectType(
      "Query",
      fields[data.Repository, Unit](
        Field(
          "blogPost",
          OptionType(BlogPost),
          arguments = UID :: Nil,
          resolve = ctx => ctx.ctx.getBlogPost(ctx arg UID).get
        ),
        Field(
          "blogPost1",
          OptionType(BlogPost),
          arguments = UID2 :: Nil,
          resolve = ctx => ctx.ctx.getBlogPost(ctx arg UID2).get
        )
      )
    )

  val PrismicSchema = Schema(Query)
}

// {
//   "Blog Post" : {
//     "uid" : {
//       "type" : "UID",
//       "config" : {
//         "placeholder" : "unique-identifier-for-blog-post-url"
//       }
//     },
//     "title" : {
//       "type" : "StructuredText",
//       "config" : {
//         "single" : "heading1",
//         "placeholder" : "Post title..."
//       }
//     },
//     "shortlede" : {
//       "type" : "StructuredText",
//       "config" : {
//         "single" : "paragraph",
//         "placeholder" : "Short lede (the shortest catcher to your topic)"
//       }
//     },
//     "image" : {
//       "type" : "Image",
//       "config" : {
//         "placeholder" : "Main blog post image",
//         "thumbnails" : [ {
//           "name" : "thumbnail",
//           "width" : 150,
//           "height" : 80
//         }, {
//           "name" : "medium",
//           "width" : 300,
//           "height" : 160
//         }, {
//           "name" : "large",
//           "width" : 640,
//           "height" : 340
//         } ]
//       }
//     },
//     "body" : {
//       "type" : "StructuredText",
//       "fieldset" : "Blog post content",
//       "config" : {
//         "placeholder" : "Start writing your blog post here...",
//         "imageConstraint" : {
//           "width" : 640
//         }
//       }
//     }
//   }
// }
