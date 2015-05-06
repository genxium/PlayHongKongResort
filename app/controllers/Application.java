package controllers;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import play.Logger;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Application extends Controller {

    public static Result index(Integer dev) {
        try {
		Content html = views.html.homepage.render(dev);
		return ok(html);
        } catch (Exception e) {

        }
        return badRequest();
    }

    public static Result admin() {
        try {
            Content html = views.html.admin.render();
            return ok(html);
        } catch (Exception e) {

        }
        return badRequest();
    }

    public static Result captcha(String sid) {
		if (sid == null) return badRequest();
        DefaultKaptcha captcha = new DefaultKaptcha();
        captcha.setConfig(new Config(new Properties()));
        String text = captcha.createText();
        BufferedImage img = captcha.createImage(text);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpg", baos);
			session(sid, text);
            baos.flush();
			return ok(baos.toByteArray()).as("image/jpg");
        } catch (IOException e) {
            Logger.debug(e.getMessage());
			return badRequest();
        }
    }
}
