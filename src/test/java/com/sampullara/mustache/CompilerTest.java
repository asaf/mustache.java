package com.sampullara.mustache;

import junit.framework.TestCase;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Tests for the compiler.
 * <p/>
 * User: sam
 * Date: May 3, 2010
 * Time: 10:23:54 AM
 */
public class CompilerTest extends TestCase {
  private File root;

  public void testSimple() throws MustacheException, IOException, ExecutionException, InterruptedException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("simple.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String name = "Chris";
      int value = 10000;

      int taxed_value() {
        return (int) (this.value - (this.value * 0.4));
      }

      boolean in_ca = true;
    }));
    writer.flush();
    assertEquals(getContents(root, "simple.txt"), sw.toString());
  }

  public void testSimple2() throws MustacheException, IOException, ExecutionException, InterruptedException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("simple.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String name = "Chris";
      int value = 10000;

      int taxed_value() {
        return (int) (this.value - (this.value * 0.4));
      }

      boolean in_ca = false;
    }));
    writer.flush();
    assertEquals(getContents(root, "simple2.txt"), sw.toString());
  }

  public void testEscaped() throws MustacheException, IOException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("escaped.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String title = "Bear > Shark";
      String entities = "&quot;";
    }));
    writer.flush();
    assertEquals(getContents(root, "escaped.txt"), sw.toString());
  }

  public void testUnescaped() throws MustacheException, IOException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("unescaped.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String title() {
        return "Bear > Shark";
      }
    }));
    writer.flush();
    assertEquals(getContents(root, "unescaped.txt"), sw.toString());
  }

  public void testInverted() throws MustacheException, IOException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("inverted_section.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String name() {
        return "Bear > Shark";
      }

      ArrayList repo = new ArrayList();
    }));
    writer.flush();
    assertEquals(getContents(root, "inverted_section.txt"), sw.toString());
  }

  public void testComments() throws MustacheException, IOException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("comments.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, new Scope(new Object() {
      String title() {
        return "A Comedy of Errors";
      }
    }));
    writer.flush();
    assertEquals(getContents(root, "comments.txt"), sw.toString());
  }

  public void testPartial() throws MustacheException, IOException {
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("template_partial.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    Scope scope = new Scope();
    scope.put("title", "Welcome");
    scope.put("template_partial_2", new Object() {
      String again = "Goodbye";
    });
    m.execute(writer, scope);
    writer.flush();
    assertEquals(getContents(root, "template_partial.txt"), sw.toString());
  }

  public void testComplex() throws MustacheException, IOException {
    Scope scope = new Scope(new Object() {
      String header = "Colors";
      List item = Arrays.asList(
              new Object() {
                String name = "red";
                boolean current = true;
                String url = "#Red";
              },
              new Object() {
                String name = "green";
                boolean current = false;
                String url = "#Green";
              },
              new Object() {
                String name = "blue";
                boolean current = false;
                String url = "#Blue";
              }
      );

      boolean link(Scope s) {
        return !((Boolean) s.get("current"));
      }

      boolean list(Scope s) {
        return ((List) s.get("item")).size() != 0;
      }

      boolean empty(Scope s) {
        return ((List) s.get("item")).size() == 0;
      }
    });
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("complex.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, scope);
    writer.flush();
    assertEquals(getContents(root, "complex.txt"), sw.toString());
  }

  public void testJson() throws IOException, MustacheException {
    String content = getContents(root, "template_partial.js");
    content = content.substring(content.indexOf("=") + 1);
    JsonParser jp = new MappingJsonFactory().createJsonParser(content);
    JsonNode jsonNode = jp.readValueAsTree();
    Scope scope = new Scope(jsonNode);
    MustacheCompiler c = new MustacheCompiler(root);
    Mustache m = c.parseFile("template_partial.html");
    StringWriter sw = new StringWriter();
    MustacheWriter writer = new MustacheWriter(sw);
    m.execute(writer, scope);
    writer.flush();
    assertEquals(getContents(root, "template_partial.txt"), sw.toString());

  }

  private String getContents(File root, String file) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(new File(root, file)));
    StringWriter capture = new StringWriter();
    char[] buffer = new char[8192];
    int read;
    while ((read = br.read(buffer)) != -1) {
      capture.write(buffer, 0, read);
    }
    return capture.toString();
  }

  protected void setUp() throws Exception {
    super.setUp();
    root = new File("src/test/resources");
  }
}