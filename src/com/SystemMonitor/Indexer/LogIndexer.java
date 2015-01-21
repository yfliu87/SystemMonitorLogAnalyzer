package com.SystemMonitor.Indexer;

/*
 * This is the key part for indexing logs like indexing and incremental indexing
 * 
 * Below things will be explicitly indexed:
 * 1. log content including toolstring, crash info
 * 2. file name
 * 3. job date
 * 4. feature usage
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import com.SystemMonitor.LogParser.ParserWrapper;
import com.SystemMonitor.Model.SystemMonitorException;

public class LogIndexer{
	private Directory _logIndexDirectory;
	private IndexReader _idxReader;
	private IndexSearcher _idxSearcher;
	
	public LogIndexer(String logFilePath, String indexFilePath) throws IOException{
		_logIndexDirectory = FSDirectory.open(new File(indexFilePath));
	}
	
	@SuppressWarnings("deprecation")
	public ScoreDoc[] searchByKeyword(String field, String keyword){
		ScoreDoc[] sds = null;
		
		try {
			if (_logIndexDirectory.listAll().length == 0)
				return sds;
		
			_idxReader = DirectoryReader.open(_logIndexDirectory);
			_idxSearcher = new IndexSearcher(_idxReader);
			QueryParser parser = new QueryParser(Version.LUCENE_4_10_2, field, new StandardAnalyzer(Version.LATEST));
			Query query = parser.parse(keyword);
			TopDocs td = _idxSearcher.search(query, 3);
			sds = td.scoreDocs; 
		} catch (Exception e) {
			SystemMonitorException.recordException(e.getMessage());
		}finally{
		}
		return sds;
	}

	public List<String> searchJobs(String featureName) throws IOException{
		ScoreDoc[] jobs = searchByKeyword("content", featureName);
		return getFileName(jobs);
	}
	
	public List<String> searchToolUsage(String toolName) throws IOException{
		ScoreDoc[] sds = searchByKeyword("content", toolName);
		return getFileName(sds);	
	}
	
	public List<String> searchCrashJobs() throws IOException{
		ScoreDoc[] sds = searchByKeyword("content", "crash");
		return getFileName(sds);	
	}
	
	public List<String> searchJobByDate(String date) throws IOException{
		ScoreDoc[] sds = searchByKeyword("date", date);
		return getFileName(sds);
	}
	
	public List<String> searchJobFeature(String featureName) throws IOException {
		ScoreDoc[] sds = searchByKeyword("feature", featureName);
		return getFileName(sds);
	}

	private List<String> getFileName(ScoreDoc[] docs) throws IOException{
		List<String> ret = new ArrayList<String>();
		
		if (docs == null){
			ret.add("No match result found! Please change query condition");
			return ret;
		}
		
		for (ScoreDoc sd : docs){
			Document document = _idxSearcher.doc(sd.doc);
			ret.add(document.get("filename"));
		}
		return ret;	
	}

}
