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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.SystemMonitor.LogParser.ParserWrapper;
import com.SystemMonitor.Model.JobInformation;
import com.SystemMonitor.Model.PatchVersionDefinition;
import com.SystemMonitor.Util.DateHelper;
import com.SystemMonitor.Util.SystemMonitorException;

public class LogIndexer{
	private String _logFilePath = null;
	private Directory _logIndexDirectory;
	private IndexReader _idxReader;
	private IndexSearcher _idxSearcher;
	private ParserWrapper _parser;
	
	public LogIndexer(String logFilePath, String indexFilePath){
		try{
			this._logFilePath = logFilePath;
			_logIndexDirectory = FSDirectory.open(new File(indexFilePath));
		}catch(IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}
	
	public LogIndexer(String logFilePath, String indexFilePath, ParserWrapper parser){
		try{
			this._logFilePath = logFilePath;
			_logIndexDirectory = FSDirectory.open(new File(indexFilePath));
			_parser = parser;
		}catch(IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}

	public void index(){

		@SuppressWarnings("deprecation")
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_2,
				new StandardAnalyzer(Version.LATEST));
		config.setOpenMode(OpenMode.CREATE);

		try {
			IndexWriter iw = new IndexWriter(_logIndexDirectory, config);
			iw.deleteAll();

			File files = new File(this._logFilePath);

			for (File file : files.listFiles()) {
				if (file.getAbsolutePath().endsWith("csv") || file.getAbsolutePath().endsWith("txt"))
					continue;

				indexFile(file, iw);
			}

			iw.commit();

			if (iw != null)
				iw.close();
		} catch (Exception e) {
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
	}
	
	private void indexFile(File file, IndexWriter iw) {
		Map<String, List<String>> jobfeatures = _parser.getFeatures(file);
		Map<String, List<String>> toolstrings = _parser.getTools(file);

		List<JobInformation> jobInfos = _parser.getJobInfo(file);
		for (JobInformation jobInfo : jobInfos) {
			Document doc = null;

			try {
				if (null != jobInfo) {
					doc = new Document();

					String mwVersion = jobInfo.getMWVersion();
					doc.add(new Field(IndexField.MWVERSION, isValidFieldCandidate(mwVersion)?mwVersion : "empty",
							TextField.TYPE_STORED));
					
//					doc.add(new StringField(IndexField.MWVERSION, 
//							isValidFieldCandidate(mwVersion)?mwVersion : "empty",
//									Field.Store.YES));
//					
					List<String> patchVersion = jobInfo.getPatches();
					for (String patch : patchVersion){
						patch = PatchVersionDefinition.convert(patch);
						
						doc.add(new StringField(IndexField.PATCHVERSION, 
								isValidFieldCandidate(patch)?patch : "empty",
										Field.Store.YES));
//						
//						doc.add(new StringField(IndexField.PATCHVERSION,
//								isValidFieldCandidate(patch) ? patch :"empty", Field.Store.YES));
					}

					String jobName = jobInfo.getJobName();
					doc.add(new StringField(IndexField.JOB_NAME, 
							isValidFieldCandidate(jobName)?jobName : "empty",
								Field.Store.YES));

					String wellName = jobInfo.getWellName();
					doc.add(new StringField(IndexField.WELL_NAME, 
							isValidFieldCandidate(wellName)? wellName : "empty",
								Field.Store.YES));

					String clientName = jobInfo.getClientName();
					doc.add(new StringField(IndexField.CLIENT_NAME,
							isValidFieldCandidate(clientName)? clientName : "empty",
							Field.Store.YES));

					String startDate = DateHelper.formatDate(
							jobInfo.getJobStartDate(), false);

					doc.add(new StringField(IndexField.STARTDATE,
								isValidFieldCandidate(startDate)? startDate.replace("/", "") : "empty",
								Field.Store.YES));

					double duration = jobInfo.getJobDuration();
					doc.add(new DoubleField(IndexField.DURATION, duration,
							Field.Store.YES));

					String workflow = jobInfo.getWorkflow();
					doc.add(new Field(IndexField.WORKFLOW, 
							isValidFieldCandidate(workflow)? workflow : "empty",
							TextField.TYPE_STORED));
					
					String unitSystem = jobInfo.getUnitSystem();
					doc.add(new Field(IndexField.UNITSYSTEM, 
							isValidFieldCandidate(unitSystem)? unitSystem : "empty",
							TextField.TYPE_STORED));

					doc.add(new Field(IndexField.CRASHED, jobInfo.isCrashed() ? "yes" : "no",
								TextField.TYPE_STORED));

					for (String process : jobInfo.getCrashProcess()){
						doc.add(new Field(IndexField.CRASHPROCESS, process,
								TextField.TYPE_STORED));
					}
					
					if (toolstrings.containsKey(jobName)) {
						List<String> toolstring = toolstrings.get(jobName);
						for (String tool : toolstring) {
							if (null != tool) {
								doc.add(new Field(IndexField.TOOL, tool,
										TextField.TYPE_STORED));

								String[] toolElements = tool.split(" |:");
								for (String toolElement : toolElements)
									doc.add(new Field(IndexField.TOOL,
											toolElement, TextField.TYPE_STORED));
							}
						}
					}

					if (jobfeatures.containsKey(jobName)) {
						List<String> jobfeature = jobfeatures.get(jobName);
						for (String feature : jobfeature) {
							if (null != feature)
								doc.add(new StringField(IndexField.FEATURE,
										feature, Field.Store.YES));
						}
					}
				}
				if (doc != null)
					iw.addDocument(doc);
		
			} catch (Exception e) {
				SystemMonitorException.logException(Thread.currentThread(), e, file);
			}
		}
	}

	private boolean isValidFieldCandidate(String targetField) {
		return targetField != null && targetField.length() > 0;
	}

	public int getTotalJobs(){
		IndexReader idxReader = null;
		int jobCount = 0;
		
		try{
			idxReader = DirectoryReader.open(_logIndexDirectory);
			jobCount = idxReader.maxDoc();
		}catch (Exception e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}finally{
			if (idxReader != null){
				try {
					idxReader.close();
				} catch (IOException e) {
					SystemMonitorException.logException(Thread.currentThread(), e);
				}
			}
		}
		return jobCount;	
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
			TopDocs td = _idxSearcher.search(query, Integer.MAX_VALUE);
			sds = td.scoreDocs; 
		} catch (Exception e) {
			SystemMonitorException.logException(Thread.currentThread(), e);
		}finally{
		}
		return sds;
	}

	public List<JobInformation> searchWorkflow(String workflow) {
		return getFileName(searchByKeyword(IndexField.WORKFLOW, workflow));
	}
	
	public List<JobInformation> searchToolUsage(String toolName){
		return getFileName(searchByKeyword(IndexField.TOOL, toolName));
	}
	
	public List<JobInformation> searchCrashJobs() {
		return getFileName(searchByKeyword(IndexField.CRASHED, "yes"));	
	}
	
	public List<JobInformation> searchJobByDate(String field, String date) {
		return getFileName(searchByKeyword(field, date));
	}
	
	public List<JobInformation> searchJobFeature(String featureName) {
		return getFileName(searchByKeyword(IndexField.FEATURE, featureName));
	}
	
	public List<JobInformation> searchMWVersion(String version){
		return getFileName(searchByKeyword(IndexField.MWVERSION, version));
	}
	
	public List<JobInformation> searchPatchVersion(String patch){
		return getFileName(searchByKeyword(IndexField.PATCHVERSION, patch));
	}

	private List<JobInformation> getFileName(ScoreDoc[] docs) {
		List<JobInformation> ret = new ArrayList<JobInformation>();
		
		if (docs == null)
			return ret;
		
		try{
			for (ScoreDoc sd : docs){
				Document document = _idxSearcher.doc(sd.doc);
				ret.add(readJobInfo(document));
			}	
		}catch(IOException e){
			SystemMonitorException.logException(Thread.currentThread(), e);
		}
		return ret;	
	}
	
	private double getDuration(String duration){
		duration = (duration == null ? "0.0" : duration);
		BigDecimal bd = new BigDecimal(Double.parseDouble(duration));
		return bd.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}

	public ArrayList<JobInformation> SearchAllDocs() {
		ArrayList<JobInformation> ret = new ArrayList<JobInformation>();
		
		try {
			if (_logIndexDirectory.listAll().length == 0)
				return ret;
		
			if (_idxReader == null)
				_idxReader = DirectoryReader.open(_logIndexDirectory);
			
			int totalDocs = _idxReader.numDocs();
			
			for (int i = 0; i < totalDocs; i++){
				Document document = _idxReader.document(i);
				ret.add(readJobInfo(document));
			}
		} catch (Exception e) {
			SystemMonitorException.logException(Thread.currentThread(), e);
		}finally{
		}
		return ret;
	}

	private JobInformation readJobInfo(Document document) {

		JobInformation candidate = new JobInformation();
		candidate.setMWVersion(document.get(IndexField.MWVERSION));
		candidate.updatePatchVersion(PatchVersionDefinition.convert(document.get(IndexField.PATCHVERSION)));
		candidate.setJobName(document.get(IndexField.JOB_NAME));
		candidate.setClientName(document.get(IndexField.CLIENT_NAME));
		candidate.setWellName(document.get(IndexField.WELL_NAME));
		candidate.setJobStartDate(DateHelper.formatCalendar(document.get(IndexField.STARTDATE)));
		candidate.setWorkflow(document.get(IndexField.WORKFLOW));
		candidate.setUnitSystem(document.get(IndexField.UNITSYSTEM));
		candidate.setJobDuration(getDuration(document.get(IndexField.DURATION)));
		candidate.setCrashed(document.get(IndexField.CRASHED).equals("yes"));
		if (candidate.isCrashed()){
			candidate.updateCrashProcess(document.get(IndexField.CRASHPROCESS));
		}
		
		return candidate;
	}
}
