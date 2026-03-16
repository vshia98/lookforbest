package com.lookforbest.controller;

import com.lookforbest.entity.Robot;
import com.lookforbest.repository.RobotCategoryRepository;
import com.lookforbest.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final RobotRepository robotRepository;
    private final RobotCategoryRepository categoryRepository;

    @Value("${app.site.base-url:https://lookforbest.com}")
    private String baseUrl;

    /** sitemap.xml 自动生成（包含机器人详情页 + 分类页） */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n");
        xml.append("        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("        xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9 ");
        xml.append("http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n");

        String today = LocalDate.now().toString();

        // 首页
        appendUrl(xml, baseUrl + "/", "1.0", "daily", today);
        appendUrl(xml, baseUrl + "/robots", "0.9", "daily", today);
        appendUrl(xml, baseUrl + "/manufacturers", "0.7", "weekly", today);

        // 分类页
        categoryRepository.findAll().forEach(cat ->
            appendUrl(xml, baseUrl + "/robots?categoryId=" + cat.getId(), "0.7", "weekly", today)
        );

        // 机器人详情页（lastmod 统一用当天，避免暴露每条记录的更新时间）
        List<Object[]> slugDates = robotRepository.findAllSlugAndUpdatedAt(Robot.RobotStatus.active);
        for (Object[] row : slugDates) {
            String slug = (String) row[0];
            appendUrl(xml, baseUrl + "/robots/" + slug, "0.8", "weekly", today);
        }

        xml.append("</urlset>");
        return ResponseEntity.ok(xml.toString());
    }

    /** robots.txt */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robotsTxt() {
        String content = "User-agent: *\n" +
                "Allow: /\n" +
                "Disallow: /api/\n" +
                "Disallow: /actuator/\n" +
                "Disallow: /swagger-ui\n" +
                "Disallow: /api-docs\n" +
                "Disallow: /admin/\n" +
                "Disallow: /login\n" +
                "Disallow: /register\n\n" +
                "Sitemap: " + baseUrl + "/sitemap.xml\n";
        return ResponseEntity.ok(content);
    }

    private void appendUrl(StringBuilder xml, String loc, String priority, String changefreq, String lastmod) {
        xml.append("  <url>\n");
        xml.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        xml.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        xml.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        xml.append("    <priority>").append(priority).append("</priority>\n");
        xml.append("  </url>\n");
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
