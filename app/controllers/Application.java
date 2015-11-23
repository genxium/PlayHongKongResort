package controllers;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import play.Logger;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Loggy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Application extends Controller {

    public static final String TAG = Application.class.getName();

    public static Result index(Integer dev, String theme) {
        try {
            final Content html = views.html.homepage.render(dev, theme);
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "index", e);
        }
        return badRequest();
    }

    public static Result callback(Integer dev, String theme, String partyName, String stateWithAction) {
        try {
            final Content html = views.html.homepage.render(dev, theme);
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "callback", e);
        }
        return badRequest();
    }

    public static Result callbackWithStateAsParam(Integer dev, String theme, String partyName) {
        try {
            final Content html = views.html.homepage.render(dev, theme);
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "callbackWithStateAsParam", e);
        }
        return badRequest();
    }

    public static Result admin(Integer dev) {
        try {
            final Content html = views.html.admin.render(dev);
            return ok(html);
        } catch (Exception e) {
            Loggy.e(TAG, "admin", e);
        }
        return badRequest();
    }

    public static Result wstest() {
        try {
            final Content html = views.html.wstest.render();
            return ok(html);
        } catch (Exception e) {

        }
        return badRequest();
    }

    public static Result captcha(String sid) {
        if (sid == null) return badRequest();
        final DefaultKaptcha captcha = new DefaultKaptcha();
        captcha.setConfig(new Config(new Properties()));
        final String text = captcha.createText();
        final BufferedImage img = captcha.createImage(text);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
