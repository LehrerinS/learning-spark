// SOLUTION for running github projects
// https://github.com/libgdx/libgdx/wiki/Gradle-and-Intellij-IDEA Run -> Edit Configurations and put SDK on JRE.
// What we get with frameworks:
// WEB SERVER - Frameworks either embed or integrate with specific web server. This is what handles the actual communication over HTTP.
// It makes sure it can handle many requests at once and serve many clients
// ROUTING - An HTTP request specifies resources it wants through headers. A framework allows developers to define what code gets run based on resource requested.
// They usually allow you to partially match URI to keep things dynamic
// REQUEST AND RESPONSE OBJECTS - Frameworks usually give you very reach request and response objects that are representations of the HTTP requests and responses.
// These object make it very easy for you to check if header exists, or handle normal HTTP status code
// TEMPLATING - Most pages have surrounding style and only little bits of dynamic data. Instead of writing all those bits in a string idea of templating is used.
// Most frameworks allows you to snap in different templating languages to build pages and push in just the dynamic bits into surrounding HTML
package com.teamtreehouse.courses1;

// The import allows the java programmer to access classes of a package without package qualification.
// The static import feature allows to access the static members of a class without the class qualification.
// The import provides accessibility to classes and interface whereas static import provides accessibility to static members of the class.
// in this example if we would import only class Spark, and no static, we could use get with Spark.get. We cannot import static only on class has to be on method of the class

import com.teamtreehouse.courses1.model.CourseIdea;
import com.teamtreehouse.courses1.model.CourseIdeaDAO;
import com.teamtreehouse.courses1.model.NotFoundException;
import com.teamtreehouse.courses1.model.SimpleCourseIdeaDAO;
import spark.ModelAndView;
import spark.Request;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    private static final String FLASH_MESSAGE_KEY = "flash_message";

    public static void main(String[] args) {
// This is for css location. Then in href in hbs files we specify only /css/main/css. I think :)
        staticFileLocation("/public");
// here as always on left side we have an interface and on the right side implementation of the interface
        CourseIdeaDAO dao = new SimpleCourseIdeaDAO();

//        the purpose of this method is to set cookie in req attribute and not to access it any more over cookie, don't know why (to untied username from cookie implementation)
        before((req, res) -> {
            if (req.cookie("username") != null) {
                req.attribute("username", req.cookie("username"));
            }
        });

        // in Spark we use before method for authentication, and we can use it more than once
        // in many frameworks this is known as MiddleWARE or filter as well
//        here "/ideas" is so called route and we use it to specify that this before should happen only for this route
//        the goal is to forbidd anyone who didn't provide username to go to the page ideas
        before("/ideas", (req, res) -> {
            if (req.attribute("username") == null) {
                setFlashMessage(req, "Whoops, please sign in first!");
                res.redirect("/");
//               halt is here to stop request from hitting any other route
                halt();
            }
        });

//      this lambda here is from Route interface, click on it to see it
//        get("/hello", (req, res) -> "Hello World");
        get("/hello", (req, res) -> {
            res.redirect("/");
            return null;
        });

//      this example is from here https://github.com/perwendel/spark-template-engines/tree/master/spark-template-handlebars. That is how you handle with new stuff
//      so this means when something comes in matching the path / run this route which at this moment builds a new model and view object and only has a view portion,
//      our template name which exist in resources/templates directory and it renders it using HandlebarsTemplateEngine
        get("/", (req, res) -> {
            Map<String, String> model = new HashMap<>();
//         we can get cookie on request by providing cookie name
            model.put("username", req.attribute("username"));
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "index.hbs");
        }, new HandlebarsTemplateEngine());

        post("/sign-in", (req, res) -> {
            Map<String, String> model = new HashMap<>();
//          request documentation we found here http://sparkjava.com/documentation#request In HTTP request that we will have in URL we would have username in a query after question mark
//            this query param "username" is coming from post form on index page
            String username = req.queryParams("username");
//          One of the ways you make HTTP appear to be stateful is by storing little bits of data on each request that is sent up to the server. These are called cookies. Let’s bake some!
//          we can set cookie on response and use it on all our future requests
//          response documentation we found under http://sparkjava.com/documentation#response
//          we store cookie in response cookie as cookie name "username" and value taken from request query params. It is posible in this framework to set cookie only on response.
//          afterwards we can get cookie on request like it is done in the example above
            res.cookie("username", username);
            model.put("username", username);
            return new ModelAndView(model, "sign-in.hbs");
        }, new HandlebarsTemplateEngine());

        get("/ideas", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("ideas", dao.findAll());
            model.put("flashMessage", captureFlashMessage(req));
            return new ModelAndView(model, "ideas.hbs");
        }, new HandlebarsTemplateEngine());

//        on ideas.hbs this one is for posting ideas
        post("/ideas", (req, res) -> {
//            here we build idea from query param title from ideas.hbs and from cookie username
            String title = req.queryParams("title");
            CourseIdea courseIdea = new CourseIdea(title, req.attribute("username"));
            dao.add(courseIdea);
//            here we use post redirect get pattern PRG, two requests are actually done at the same time
            res.redirect("/ideas");
            return null;
        });

        get("/ideas/:slug", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("idea", dao.findBySlug(req.params("slug")));
            return new ModelAndView(model, "idea.hbs");
        }, new HandlebarsTemplateEngine());

//        post("/ideas/:slug/vote", (req, res) -> {
//            CourseIdea idea = dao.findBySlug(req.params("slug"));
//            idea.addVoter(req.attribute("username"));
//            res.redirect("/ideas");
//            return null;
//        });

//        on ideas.hbs this one is for posting votes
        post("/ideas/:slug/vote", (req, res) -> {
            CourseIdea idea = dao.findBySlug(req.params("slug"));
            boolean added = idea.addVoter(req.attribute("username"));
            if (added) {
                setFlashMessage(req, "Thanks for your vote!");
            } else {
                setFlashMessage(req, "You already voted!");
            }
            res.redirect("/ideas");
            return null;
        });

//        exception method is built in in Spark framework and we will use it for displaying flash messages, because spark framework doesn't have flash messages
//        it takes class and it handles exc, req and res
        exception(NotFoundException.class, (exc, req, res) -> {
            res.status(404);
//            exception behavious a little bit differently then post, get... and we create engine
            HandlebarsTemplateEngine engine = new HandlebarsTemplateEngine();
//            and then we do exactly the same like it is automatically done above we do render on request which gives back string
            String html = engine.render(
                    new ModelAndView(null, "not-found.hbs"));
//            and then we return the body of response
            res.body(html);
        });

    }

// sessions are key value data store that are available almost on all frameworks. it is kind of like a pocket that you can put things to
    private static void setFlashMessage(Request req, String message) {
//        we access to the session from the req and here we set key (FLASH_MESSAGE_KEY) value (message) in attribute
        req.session().attribute(FLASH_MESSAGE_KEY, message);
    }

    private static String getFlashMessage(Request req) {
        if (req.session(false) == null) {
            return null;
        }
        if (!req.session().attributes().contains(FLASH_MESSAGE_KEY)) {
            return null;
        }
//        we cast because it is value is an object
        return (String) req.session().attribute(FLASH_MESSAGE_KEY);
    }

    private static String captureFlashMessage(Request req) {
        String message = getFlashMessage(req);
        if (message != null) {
            req.session().removeAttribute(FLASH_MESSAGE_KEY);
        }
        return message;
    }
}
