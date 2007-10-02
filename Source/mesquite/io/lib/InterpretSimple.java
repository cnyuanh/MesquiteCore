/* Mesquite (package mesquite.io).  Copyright 2000-2007 D. Maddison and W. Maddison. Version 2.0, September 2007.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.io.lib;/*~~  */import java.util.*;import java.awt.*;import mesquite.lib.*;import mesquite.lib.characters.*;import mesquite.lib.duties.*;import mesquite.categ.lib.*;import mesquite.cont.lib.*;/* ============  a file interpreter for Simple Text files ============*/public abstract class InterpretSimple extends FileInterpreterI {	Class[] acceptedClasses;/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) {		acceptedClasses = new Class[] {ProteinState.class, DNAState.class, CategoricalState.class, ContinuousState.class, GeographicState.class}; 		return true;  //make this depend on taxa reader being found?)  	 }  	 /*.................................................................................................................*/	public boolean canExportEver() {  		 return true;  //	}/*.................................................................................................................*/	public boolean canExportProject(MesquiteProject project) {  		 return project.getNumberCharMatrices(acceptedClasses) > 0;  //	}/*.................................................................................................................*/	public boolean canExportData(Class dataClass) {  		 for (int i = 0; i<acceptedClasses.length; i++)		 	if (dataClass==acceptedClasses[i])		 		return true;		 return false; 	}/*.................................................................................................................*/	public boolean canImport() {  		 return true;	}		public abstract boolean isCategorical ();/*.................................................................................................................*/	public abstract CharacterData createData(CharactersManager charTask, Taxa taxa);/*.................................................................................................................*/	public void readFile(MesquiteProject mf, MesquiteFile file, String arguments) {		incrementMenuResetSuppression();		ProgressIndicator progIndicator = new ProgressIndicator(mf,"Importing File "+ file.getName(), file.existingLength());		progIndicator.start();		file.linkProgressIndicator(progIndicator);		if (file.openReading()) {			TaxaManager taxaTask = (TaxaManager)findElementManager(Taxa.class);			 CharactersManager charTask = (CharactersManager)findElementManager(CharacterData.class);						Taxa taxa = taxaTask.makeNewTaxa(getProject().getTaxas().getUniqueName("Untitled Block of Taxa"), 0, false);			taxa.addToFile(file, getProject(), taxaTask);			CharacterData data = (CharacterData)createData(charTask,taxa);			data.addToFile(file, getProject(), null);			boolean wassave = data.saveChangeHistory;			data.saveChangeHistory = false;						int numTaxa = 0;			StringBuffer sb = new StringBuffer(1000);			file.readLine(sb);			String line = sb.toString();			String token;			MesquiteString result = new MesquiteString();			char c;						boolean abort = false;			while (!StringUtil.blank(line) && !abort) {				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0												token = parser.getNextToken();  //taxon Name				if (!StringUtil.blank(token)) {					taxa.addTaxa(numTaxa-1, 1, true);					Taxon t = taxa.getTaxon(numTaxa);										if (t!=null) {						t.setName(token);						progIndicator.setText("Reading taxon: "+token);						int ic = 0;												if (isCategorical()) {							while (parser.getPosition()<line.length()) {								c=parser.nextDarkChar();								if (c=='\0')									break;								if (data.getNumChars() <= ic) {									data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed								}								((CategoricalData)data).setState(ic, numTaxa, c);    // setting state to that specified by character c								ic += 1;							}						} else {  // continuous data							while (parser.getPosition()<line.length()) {								token = parser.getNextToken();								if (StringUtil.blank(token))									break;								if (data.getNumChars() <= ic) {									data.addCharacters(data.getNumChars()-1, 1, false);   // add a character if needed								}								double value = MesquiteDouble.fromString(token);								if (MesquiteDouble.isCombinable(value))									((ContinuousData)data).setState(ic, numTaxa, 0, value);								ic += 1;							}						}											}					numTaxa++;				}				file.readLine(sb);				line = sb.toString();						if (file.getFileAborted()) {					abort = true;				}			}			data.saveChangeHistory = wassave;			data.resetChangedSinceSave();			finishImport(progIndicator, file, abort);		}		decrementMenuResetSuppression();	}	/* ============================  exporting ============================*/	boolean includeGaps = false;	boolean includeTabs = false;	boolean mesquiteReadable = false;	/*.................................................................................................................*/		public boolean getExportOptions(boolean dataSelected, boolean taxaSelected){		MesquiteInteger buttonPressed = new MesquiteInteger(1);		ExporterDialog exportDialog = new ExporterDialog(this,containerOfModule(), "Export Simple Table Options", buttonPressed);				Checkbox includeGapsCheckBox = exportDialog.addCheckBox("include gaps", includeGaps);		Checkbox includeTabsCheckBox = exportDialog.addCheckBox("tab-delimited", includeTabs);		Checkbox mesquiteReadableCheckBox = exportDialog.addCheckBox("Mesquite readable", mesquiteReadable);				exportDialog.completeAndShowDialog(dataSelected, taxaSelected);					boolean ok = (exportDialog.query(dataSelected, taxaSelected)==0);				includeGaps = includeGapsCheckBox.getState();		includeTabs = includeTabsCheckBox.getState();		mesquiteReadable = mesquiteReadableCheckBox.getState();		exportDialog.dispose();		return ok;	}		/*.................................................................................................................*/		public boolean getExportOptionsSimple(boolean dataSelected, boolean taxaSelected){   // an example of a simple query, that only proved line delimiter choice; not used here		return (ExporterDialog.query(this,containerOfModule(), "Export Simple Table Options")==0);	}		/*.................................................................................................................*/	public abstract CharacterData findDataToExport(MesquiteFile file, String arguments);	/*.................................................................................................................*/	public void exportFile(MesquiteFile file, String arguments) { //if file is null, consider whole project open to export		Arguments args = new Arguments(new Parser(arguments), true);		boolean usePrevious = args.parameterExists("usePrevious");		CharacterData data = findDataToExport(file, arguments);		if (data ==null) {			showLogWindow(true);			logln("WARNING: No suitable data available for export to a file of format \"" + getName() + "\".  The file will not be written.\n");			return;		}		Taxa taxa = data.getTaxa();		if (!MesquiteThread.isScripting() && !usePrevious)			if (!getExportOptions(data.anySelected(), taxa.anySelected()))				return;		int expectedIncrement = 1;		if (includeTabs)			expectedIncrement++;		int numTaxa = taxa.getNumTaxa();		int numChars = data.getNumChars();		StringBuffer outputBuffer = new StringBuffer(numTaxa*(20 + numChars));				for (int it = 0; it<numTaxa; it++){			if (!writeOnlySelectedTaxa || (taxa.getSelected(it))){				if (includeTabs) 					outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)));				else					outputBuffer.append(ParseUtil.tokenize(taxa.getTaxonName(it)) + "\t");				for (int ic = 0; ic<numChars; ic++) {					if (!writeOnlySelectedData || (data.getSelected(ic))){						int currentSize = outputBuffer.length();						if (includeGaps || (!data.isInapplicable(ic,it))) {								if (includeTabs) outputBuffer.append("\t");								data.statesIntoStringBuffer(ic, it, outputBuffer, false);						}						int ob = outputBuffer.length();						if ((outputBuffer.length()-currentSize>expectedIncrement)&& mesquiteReadable) {							alert("Sorry, this data matrix can't be exported to this format (some character states aren't represented by a single symbol [char. " + CharacterStates.toExternal(ic) + ", taxon " + Taxon.toExternal(it) + "])");							return;						}					}				}				outputBuffer.append(getLineEnding());			}		}				saveExportedFileWithExtension(outputBuffer, arguments, "txt");	}	/*.................................................................................................................*/    	 public String getName() {		return "Simple table file";   	 }	/*.................................................................................................................*/   	  	/** returns an explanation of what the module does.*/ 	public String getExplanation() { 		return "Imports and exports simple files of molecular sequence or other categorical data." ;   	 }	/*.................................................................................................................*/   	    	 }	