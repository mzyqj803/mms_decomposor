package com.mms.service;

import com.mms.dto.FastenerSimilarityResult;
import com.mms.entity.FastenerWarehouse;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 紧固件Lucene索引服务
 * 使用TF-IDF算法进行相似度搜索
 */
@Slf4j
@Service
public class FastenerLuceneIndexService {

    @Autowired
    private FastenerWarehouseService fastenerWarehouseService;

    private static final String INDEX_DIR = "lucene-index";
    private static final String FASTENER_FACTORS_FIELD = "fastener_factors";
    private static final String ID_FIELD = "id";
    
    private Directory directory;
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private IndexSearcher indexSearcher;
    private IndexReader indexReader;

    @PostConstruct
    public void init() {
        try {
            // 创建索引目录
            directory = FSDirectory.open(Paths.get(INDEX_DIR));
            analyzer = new StandardAnalyzer();
            
            // 初始化索引写入器
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, config);
            
            // 构建初始索引
            buildIndex();
            
            log.info("Lucene索引服务初始化完成");
        } catch (IOException e) {
            log.error("初始化Lucene索引服务失败", e);
            throw new RuntimeException("初始化Lucene索引服务失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (indexWriter != null) {
                indexWriter.close();
            }
            if (indexReader != null) {
                indexReader.close();
            }
            if (directory != null) {
                directory.close();
            }
            log.info("Lucene索引服务已关闭");
        } catch (IOException e) {
            log.error("关闭Lucene索引服务失败", e);
        }
    }

    /**
     * 构建紧固件索引
     */
    public void buildIndex() {
        try {
            log.info("开始构建紧固件索引...");
            
            // 清空现有索引
            indexWriter.deleteAll();
            
            // 获取所有紧固件数据
            List<FastenerWarehouse> fasteners = fastenerWarehouseService.getAllFasteners();
            
            // 为每个紧固件创建文档
            for (FastenerWarehouse fastener : fasteners) {
                Document doc = createDocument(fastener);
                indexWriter.addDocument(doc);
            }
            
            // 提交索引
            indexWriter.commit();
            
            // 重新打开索引读取器
            refreshIndexReader();
            
            log.info("紧固件索引构建完成，共索引 {} 条记录", fasteners.size());
        } catch (IOException e) {
            log.error("构建紧固件索引失败", e);
            throw new RuntimeException("构建紧固件索引失败", e);
        }
    }

    /**
     * 创建Lucene文档
     */
    private Document createDocument(FastenerWarehouse fastener) {
        Document doc = new Document();
        
        // 添加ID字段
        doc.add(new LongPoint(ID_FIELD, fastener.getId()));
        doc.add(new StoredField(ID_FIELD, fastener.getId()));
        
        // 构建fastener_factors字段
        String fastenerFactors = buildFastenerFactors(fastener);
        doc.add(new TextField(FASTENER_FACTORS_FIELD, fastenerFactors, Field.Store.YES));
        
        return doc;
    }

    /**
     * 构建紧固件因子字符串
     * 格式: <product_code>_<specs>_<level>_<name>_<Surface_Treatment>_<Material>
     */
    private String buildFastenerFactors(FastenerWarehouse fastener) {
        StringBuilder factors = new StringBuilder();
        
        factors.append(Optional.ofNullable(fastener.getProductCode()).orElse(""));
        factors.append("_");
        factors.append(Optional.ofNullable(fastener.getSpecs()).orElse(""));
        factors.append("_");
        factors.append(Optional.ofNullable(fastener.getLevel()).orElse(""));
        factors.append("_");
        factors.append(Optional.ofNullable(fastener.getName()).orElse(""));
        factors.append("_");
        factors.append(Optional.ofNullable(fastener.getSurfaceTreatment()).orElse(""));
        factors.append("_");
        factors.append(Optional.ofNullable(fastener.getMaterial()).orElse(""));
        
        return factors.toString();
    }

    /**
     * 刷新索引读取器
     */
    private void refreshIndexReader() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
        indexReader = DirectoryReader.open(directory);
        indexSearcher = new IndexSearcher(indexReader);
    }

    /**
     * 搜索相似的紧固件
     */
    public List<FastenerSimilarityResult> searchSimilarFasteners(String queryText, int maxResults) {
        try {
            if (indexSearcher == null) {
                refreshIndexReader();
            }
            
            // 创建查询解析器
            QueryParser parser = new QueryParser(FASTENER_FACTORS_FIELD, analyzer);
            
            // 解析查询文本
            Query query = parser.parse(QueryParser.escape(queryText));
            
            // 执行搜索
            TopDocs topDocs = indexSearcher.search(query, maxResults);
            
            // 转换搜索结果
            List<FastenerSimilarityResult> results = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = indexSearcher.storedFields().document(scoreDoc.doc);
                Long fastenerId = Long.parseLong(doc.get(ID_FIELD));
                
                // 获取完整的紧固件信息
                FastenerWarehouse fastener = fastenerWarehouseService.getFastenerById(fastenerId);
                if (fastener != null) {
                    FastenerSimilarityResult result = FastenerSimilarityResult.builder()
                            .id(fastener.getId())
                            .erpCode(fastener.getErpCode())
                            .specs(fastener.getSpecs())
                            .productCode(fastener.getProductCode())
                            .name(fastener.getName())
                            .level(fastener.getLevel())
                            .material(fastener.getMaterial())
                            .surfaceTreatment(fastener.getSurfaceTreatment())
                            .remark(fastener.getRemark())
                            .similarityScore(scoreDoc.score)
                            .defaultFlag(fastener.getDefaultFlag())
                            .build();
                    results.add(result);
                }
            }
            
            log.info("搜索查询 '{}' 返回 {} 条结果", queryText, results.size());
            return results;
            
        } catch (ParseException | IOException e) {
            log.error("搜索相似紧固件失败: {}", queryText, e);
            throw new RuntimeException("搜索相似紧固件失败", e);
        }
    }

    /**
     * 根据紧固件ID搜索相似紧固件
     */
    public List<FastenerSimilarityResult> searchSimilarFastenersById(Long fastenerId, int maxResults) {
        try {
            // 获取目标紧固件
            FastenerWarehouse targetFastener = fastenerWarehouseService.getFastenerById(fastenerId);
            if (targetFastener == null) {
                log.warn("未找到ID为 {} 的紧固件", fastenerId);
                return new ArrayList<>();
            }
            
            // 构建搜索查询
            String queryText = buildFastenerFactors(targetFastener);
            
            // 执行搜索
            List<FastenerSimilarityResult> results = searchSimilarFasteners(queryText, maxResults + 1);
            
            // 移除目标紧固件本身
            results.removeIf(result -> result.getId().equals(fastenerId));
            
            // 限制结果数量
            if (results.size() > maxResults) {
                results = results.subList(0, maxResults);
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("根据ID搜索相似紧固件失败: {}", fastenerId, e);
            throw new RuntimeException("根据ID搜索相似紧固件失败", e);
        }
    }

    /**
     * 重新构建索引
     */
    public void rebuildIndex() {
        buildIndex();
    }

    /**
     * 检查索引是否已构建
     */
    public boolean isIndexBuilt() {
        return indexReader != null && indexReader.numDocs() > 0;
    }

    /**
     * 获取索引文档数量
     */
    public long getIndexedDocumentCount() {
        return indexReader != null ? indexReader.numDocs() : 0;
    }
}
