package dk.patientassist.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import dk.patientassist.service.dto.ExamTreatCategoryDTO;
import dk.patientassist.service.dto.ExamTreatDTO;
import dk.patientassist.service.dto.ExamTreatTypeDTO;

/**
 * WebScrape
 */
public class WebScraper {

    static final Logger logger = LoggerFactory.getLogger(WebScraper.class);

    public static void scrapeExamsAndTreatmentHeadless(String linksAndTitlesFilePath)
            throws IOException, URISyntaxException, InterruptedException {

        URL urlOfFilePath = WebScraper.class.getClassLoader().getResource(linksAndTitlesFilePath);
        List<String> lines = Files.readAllLines(Paths.get(urlOfFilePath.toURI()));

        System.out.println("WebScraper.scrapeExamsAndTreatment() : " + urlOfFilePath.toExternalForm());

        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless");
        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

        List<ExamTreatCategoryDTO> ETCats = new ArrayList<>();

        for (var line : lines) {
            String urlStr = line.substring(0, line.indexOf(" "));
            String titleStr = line.substring(line.indexOf(" ") + 1);
            if (urlStr.length() < 1 || titleStr.length() < 1) {
                throw new IOException("input format error");
            }

            ExamTreatCategoryDTO ETCat = new ExamTreatCategoryDTO(titleStr);

            List<ExamTreatTypeDTO> ETTypes = new ArrayList<>();

            webDriver.get(urlStr);

            System.out.println("=======================================================");
            System.out.println("=======================================================");

            System.out.println("URL : " + urlStr);

            List<WebElement> details = webDriver.findElements(By.tagName("details"));
            System.out.println("found " + details.size() + " sub-categories...");

            for (var det : details) {
                String summaryTxt = det.findElement(By.tagName("summary")).getText();

                ExamTreatTypeDTO ETType = new ExamTreatTypeDTO(summaryTxt);

                List<ExamTreatDTO> ETs = new ArrayList<>();

                var ul = det.findElement(By.tagName("ul"));
                var lis = ul.findElements(By.tagName("li"));
                System.out.println("\tfound " + lis.size() + " articles...");

                for (var li : lis) {
                    var a = li.findElement(By.tagName("a"));
                    var aInnerHTML = a.getAttribute("innerHTML");
                    var aTxt = a.getAttribute("innerHTML")
                            .substring(aInnerHTML.indexOf("</span>") + "</span>".length());

                    ExamTreatDTO ET = new ExamTreatDTO(aTxt, a.getAttribute("href"));

                    ETs.add(ET);
                }

                ETType.examTreats = new ExamTreatDTO[ETs.size()];
                for (int i = 0; i < ETs.size(); i++) {
                    ETType.examTreats[i] = ETs.get(i);
                }
                ETTypes.add(ETType);
            }

            ETCat.examTreatTypes = new ExamTreatTypeDTO[ETTypes.size()];
            for (int i = 0; i < ETTypes.size(); i++) {
                ETCat.examTreatTypes[i] = ETTypes.get(i);
            }
            ETCats.add(ETCat);
        }

        int totalCount = 0, count = 0;
        for (var ETCat : ETCats) {
            for (var ETType : ETCat.examTreatTypes) {
                totalCount += ETType.examTreats.length;
            }
        }

        System.out.println("=======================================================");
        System.out.println("Fetching articles...");
        for (var ETCat : ETCats) {
            for (var ETType : ETCat.examTreatTypes) {
                for (var ET : ETType.examTreats) {
                    webDriver.get(ET.srcUrl);
                    System.out.println("\tURL : " + ET.srcUrl);

                    var title = webDriver.findElement(By.tagName("h1")).getText();
                    var content = "";

                    for (var div : webDriver.findElements(By.tagName("div"))) {
                        String dfn = div.getAttribute("data-field-name");
                        if (dfn != null && dfn.equals("PublishingPageContent")) {
                            content = div.getAttribute("innerHTML");
                        }
                    }

                    ET.article = String.format("<h1>%s</h1>%s", title, content);
                    ++count;

                    Element testValidHTML = Jsoup.parse(ET.article, "", Parser.xmlParser());
                    if (testValidHTML == null || testValidHTML.childrenSize() < 1) {
                        logger.warn("invalid article fetched from: {}", ET.srcUrl);
                        ET.article = null;
                        continue;
                    }

                    System.out.printf("\tfetched article titled \"%s\" (%d chars) (%d/%d done)%n",
                            title, ET.article.length(), count, totalCount);
                }
            }
        }
        System.out.println("=======================================================");

        try (FileWriter fw = new FileWriter("src/main/resources/data/exam_treatment_data.json", false)) {
            fw.write(Utils.getObjectMapperPretty().writeValueAsString(ETCats));
        }

        webDriver.quit();
    }

}
