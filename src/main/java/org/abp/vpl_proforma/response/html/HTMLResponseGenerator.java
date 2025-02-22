package org.abp.vpl_proforma.response.html;

import org.abp.vpl_proforma.response.gradingstructure.CombineNode;
import org.w3c.dom.*;
import proforma.xml21.FeedbackType;
import proforma.xml21.SeparateTestFeedbackType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Base64;
import java.util.List;

public class HTMLResponseGenerator {

    private Document studentFeedbackStream;
    private Document teacherFeedbackStream;

    public HTMLResponseGenerator() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.studentFeedbackStream = docBuilder.newDocument();
            this.teacherFeedbackStream = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void outputMergedTestFeedback(String studentFeedback, String teacherFeedback, double grade) {
        outputStandardHeader();
        
        System.out.println("///////////////////////////////");
        System.out.println("/// Student feedback //////////");
        System.out.println("///////////////////////////////");
        System.out.println("<|--");
        System.out.println("data:text/html;base64," + Base64.getEncoder().encodeToString(studentFeedback.getBytes()));
        System.out.println("--|>");

        System.out.println("///////////////////////////////");
        System.out.println("/// Teacher feedback //////////");
        System.out.println("///////////////////////////////");
        System.out.println("data:text/html;base64," + Base64.getEncoder().encodeToString(teacherFeedback.getBytes()));

        System.out.println("Grade :=>>" + grade);
    }

    public void generateSeparateTestFeedbackDocuments(SeparateTestFeedbackType separateTestFeedback) {
        // Initialize base HTML structure for both streams
        Element htmlTeacher = initializeHtmlDocument(teacherFeedbackStream);
        Element htmlStudent = initializeHtmlDocument(studentFeedbackStream);
    
        // Add common head elements (meta, title, style, script)
        addHeadSection(teacherFeedbackStream, htmlTeacher);
        addHeadSection(studentFeedbackStream, htmlStudent);
    
        // Add body and create main structure
        Element bodyTeacher = addBodySection(teacherFeedbackStream, htmlTeacher);
        Element bodyStudent = addBodySection(studentFeedbackStream, htmlStudent);
    
        // Add summarized feedback section
        Element studentContentDivTeacherStream = addCollapsibleFeedbackSection(teacherFeedbackStream, bodyTeacher, "Summarized Feedback");
        Element studentContentDiv = addCollapsibleFeedbackSection(studentFeedbackStream, bodyStudent, "Summarized Feedback");
    
        // Add student feedback to both streams
        addStudentFeedback(studentContentDivTeacherStream, studentContentDiv, separateTestFeedback);
    
        Element teacherContentDiv = addCollapsibleFeedbackSection(teacherFeedbackStream, bodyTeacher, "Summarized Teacher Feedback");

        // Add teacher feedback only to teacher stream
        addTeacherFeedback(teacherContentDiv, separateTestFeedback);
    
        // Add detailed feedback section
        addCollapsibleFeedbackSection(teacherFeedbackStream, bodyTeacher, "Detailed Feedback");
        addCollapsibleFeedbackSection(studentFeedbackStream, bodyStudent, "Detailed Feedback");
    }


    public void generateSeparateTestFeedbackDetailedFeedback(CombineNode gradingStructure) {
        // TODO: Implement the logic to generate detailed feedback for each test
    }

    public void outputSeparateTestFeedbackDocuments() {
        try {
            String teacherHtml = convertDocumentToString(teacherFeedbackStream);
            String studentHtml = convertDocumentToString(studentFeedbackStream);
    
            outputStandardHeader();

            // Output student feedback
            System.out.println("///////////////////////////////");
            System.out.println("/// Student feedback //////////");
            System.out.println("///////////////////////////////");
            System.out.println("<|--");
            System.out.println("data:text/html;base64," + Base64.getEncoder().encodeToString(studentHtml.getBytes()));
            System.out.println("--|>");
    
            // Output teacher feedback
            System.out.println("///////////////////////////////");
            System.out.println("/// Teacher feedback //////////");
            System.out.println("///////////////////////////////");
            System.out.println("data:text/html;base64," + Base64.getEncoder().encodeToString(teacherHtml.getBytes()));
    
            System.out.println("Grade :=>>" + 0);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void outputStandardHeader() {
        System.out.println("<|--");
        System.out.println("━━━━ EVALUATION REPORT ━━━━\n");
        System.out.println("--|>");
        System.out.println("<|--");
        System.out.println("-To access the grading results, please follow the following steps:");
        System.out.println("1. Triple-click the text provided below to select it fully.");
        System.out.println("2. Right-click the selected text and choose \"Copy,\" or simply press `Ctrl+C` on your keyboard to copy the text.");
        System.out.println("3. Navigate to your web browser and focus on the URL bar at the top. You can do this by clicking on the URL bar or pressing `Ctrl+L` on your keyboard.");
        System.out.println("4. Right-click inside the URL bar and select \"Paste,\" or press `Ctrl+V` on your keyboard to paste the previously copied text.");
        System.out.println("5. Press `Enter` to navigate to the link and view your grading results.\n");
        System.out.println("-Text to copy:");
        System.out.println("--|>");
    }

    // ... [Include all the private HTML helper methods from ProformaResponseFormatter]
    private Element initializeHtmlDocument(Document doc) {
        Element html = doc.createElement("html");
        doc.appendChild(html);
        return html;
    }
    
    private Element addHeadSection(Document doc, Element html) {
        Element head = doc.createElement("head");
        html.appendChild(head);
    
        // Add meta
        Element meta = doc.createElement("meta");
        meta.setAttribute("charset", "UTF-8");
        head.appendChild(meta);
    
        // Add title
        Element title = doc.createElement("title");
        title.appendChild(doc.createTextNode("Evaluation Report"));
        head.appendChild(title);
    
        // Add style
        Element style = doc.createElement("style");
        style.appendChild(doc.createTextNode(getCommonStyles()));
        head.appendChild(style);
    
        // Add script
        Element script = doc.createElement("script");
        script.appendChild(doc.createTextNode(getCommonScript()));
        head.appendChild(script);
    
        return head;
    }
    
    private Element addBodySection(Document doc, Element html) {
        Element body = doc.createElement("body");
        html.appendChild(body);
        return body;
    }
    
    private Element addCollapsibleFeedbackSection(Document doc, Element parent, String title) {
        Element button = doc.createElement("button");
        button.setAttribute("class", "collapsible");
        
        Element h1 = doc.createElement("h1");
        h1.appendChild(doc.createTextNode(title));
        button.appendChild(h1);
        parent.appendChild(button);
    
        Element contentDiv = doc.createElement("div");
        contentDiv.setAttribute("class", "content");
        parent.appendChild(contentDiv);
    
        return contentDiv;
    }
    
    private void addStudentFeedback(Element teacherDiv, Element studentDiv, SeparateTestFeedbackType separateTestFeedback) {
        addFeedbackSection(teacherFeedbackStream, teacherDiv, "Student Feedback", 
            separateTestFeedback.getSubmissionFeedbackList().getStudentFeedback());
        addFeedbackSection(studentFeedbackStream, studentDiv, "Student Feedback", 
            separateTestFeedback.getSubmissionFeedbackList().getStudentFeedback());
    }
    
    private void addTeacherFeedback(Element teacherDiv, SeparateTestFeedbackType separateTestFeedback) {
        addFeedbackSection(teacherFeedbackStream, teacherDiv, "Teacher Feedback", 
            separateTestFeedback.getSubmissionFeedbackList().getTeacherFeedback());
    }
    
    private void addFeedbackSection(Document doc, Element parent, String title, List<FeedbackType> feedbackList) {
        Element h2 = doc.createElement("h2");
        h2.appendChild(doc.createTextNode(title));
        parent.appendChild(h2);
    
        Element feedbackDiv = doc.createElement("div");
        feedbackDiv.setAttribute("class", "feedback");
        parent.appendChild(feedbackDiv);
    
        for (FeedbackType feedback : feedbackList) {
            feedbackDiv.appendChild(doc.createCDATASection(feedback.getContent().getValue()));
        }
    }
    
    private String getCommonStyles() {
        return  
            "h1, h2 { color: navy; margin-bottom: 0; }" +
            "h3 { font-style: italic;}" +
            ".feedback, .teacher-feedback { margin-bottom: 5; }" +
            ".grading-node {padding-left: 1px; padding-bottom: 1px;}" +
            ".indent-0 { margin-left: 0px; background-color: #b0b0b0}" +
            ".indent-1 { margin-left: 20px; background-color: #c0c0c0;}" +
            ".indent-2 { margin-left: 40px; background-color: #d0d0d0;}" +
            ".indent-3 { margin-left: 60px; background-color: #e0e0e0;}" +
            ".indent-4 { margin-left: 80px; background-color: #f0f0f0;}" +
            ".indent-5 { margin-left: 100px; background-color: #f9f9f9;}" +
            ".collapsible { cursor: pointer; padding: 10px; width: 100%; border: none; text-align: left; outline: none; font-size: 15px; }" +
            ".active, .collapsible:hover { background-color: #555; }" +
            ".content { padding: 0 18px; display: none; overflow: hidden; }" +
            ".nullify-reason {color: red; font-style: italic;}";
    }
    
    private String getCommonScript() {
        return 
            "document.addEventListener(\"DOMContentLoaded\", function() {" +
            "  var coll = document.getElementsByClassName(\"collapsible\");" +
            "  var i;" +
            "  for (i = 0; i < coll.length; i++) {" +
            "    coll[i].addEventListener(\"click\", function() {" +
            "      this.classList.toggle(\"active\");" +
            "      var content = this.nextElementSibling;" +
            "      if (content.style.display === \"block\") {" +
            "        content.style.display = \"none\";" +
            "      } else {" +
            "        content.style.display = \"block\";" +
            "      }" +
            "    });" +
            "  }" +
            "});";
    }

    private String convertDocumentToString(Document doc) throws TransformerException {
        StringWriter writer = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
