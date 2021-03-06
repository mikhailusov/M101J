package course;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BlogPostDAO {
    MongoCollection<Document> postsCollection;

    public BlogPostDAO(final MongoDatabase blogDatabase) {
        postsCollection = blogDatabase.getCollection("posts");
    }

    // Return a single post corresponding to a permalink
    public Document findByPermalink(String permalink) {

        Document post = postsCollection.find(eq("permalink", permalink)).first();

        return post;
    }

    // Return a list of posts in descending order. Limit determines
    // how many posts are returned.
    public List<Document> findByDateDescending(int limit) {

        // Return a list of Documents, each one a post from the posts collection
        List<Document> posts = postsCollection
                .find()
                .sort(new Document("date", -1))
                .limit(limit)
                .into(new ArrayList<Document>());

        return posts;
    }


    public String addPost(String title, String body, List tags, String username) {

        System.out.println("inserting blog entry " + title + " " + body);

        String permalink = title.replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();
        permalink = permalink+ (new Date()).getTime();

        // Build the post object and insert it
        Document post = new Document();
        post.append("title", title);
        post.append("body", body);
        post.append("tags", tags);
        post.append("comments", new ArrayList());
        post.append("date", new Date());
        post.append("permalink", permalink);
        post.append("author", username);

        postsCollection.insertOne(post);

        return permalink;
    }

    // Append a comment to a blog post
    public void addPostComment(final String name, final String email, final String body,
                               final String permalink) {

        // todo  XXX
        // Hints:
        // - email is optional and may come in NULL. Check for that.
        // - best solution uses an update command to the database and a suitable
        //   operator to append the comment on to any existing list of comments
        Document comment = new Document();
        comment.append("author", name);
        comment.append("email", (email != null) ? email : "");
        comment.append("body", body);

        Document post = findByPermalink(permalink);
        postsCollection.updateOne(post, new Document("$push", new Document("comments", comment)));

    }
}
