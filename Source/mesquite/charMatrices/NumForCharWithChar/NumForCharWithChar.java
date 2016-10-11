/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.NumForCharWithChar;

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
public class NumForCharWithChar extends NumberForCharacter implements Incrementable{
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2Characters.class, getName() + " needs a particular method to calculate a value pertaining to two characters (e.g., a correlation).",
		"You can request what the value for the two characters initially, or later under the Values submenu.");
	}
	NumberFor2Characters numberTask;
	CharSourceCoordObed characterSourceTask;
	Taxa taxa;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	int currentChar = 0;
	Class taskClass = NumberFor2Characters.class;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (arguments !=null) {
			numberTask = (NumberFor2Characters)hireNamedEmployee(NumberFor2Characters.class, arguments);
			if (numberTask == null)
				return sorry(getName() + " couldn't start because the requested calculator module wasn't successfully hired.");
		}
		else {
			numberTask = (NumberFor2Characters)hireEmployee(NumberFor2Characters.class, "Value to calculate for character with other character");
			if (numberTask == null)
				return sorry(getName() + " couldn't start because no calculator module obtained.");
		}

		ntC =makeCommand("setNumberTask",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberFor2Characters.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Values", ntC, NumberFor2Characters.class);
			mss.setSelected(numberTaskName);
		}
		characterSourceTask = (CharSourceCoordObed)hireEmployee(CharSourceCoordObed.class, "Source of other characters (for " + numberTask.getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		addMenuItem( "Next Character", makeCommand("nextCharacter",  this));
		addMenuItem( "Previous Character", makeCommand("previousCharacter",  this));
		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
		addMenuSeparator();

		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		if (employee == characterSourceTask || employee == numberTask)  // character source quit and none rehired automatically
			iQuit();
	}
	/*.................................................................................................................*/
	public void setCurrent(long i){
		if (characterSourceTask==null || taxa==null)
			return;
		if ((i>=0) && (i<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
			currentChar = (int)i;
		}
	}
	public String getItemTypeName(){
		return "Character";
	}
	/*.................................................................................................................*/
	public long toInternal(long i){
		return(CharacterStates.toInternal((int)i));
	}
	/*.................................................................................................................*/
	public long toExternal(long i){
		return(CharacterStates.toExternal((int)i));
	}
	/*.................................................................................................................*/
	public long getCurrent(){
		return currentChar;
	}
	/*.................................................................................................................*/
	public long getMin(){
		return 0;
	}
	/*.................................................................................................................*/
	public long getMax(){
		if (characterSourceTask==null || taxa==null)
			return 0;
		return characterSourceTask.getNumberOfCharacters(taxa)-1;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setNumberTask ", numberTask);  //TODO: note not snapshotting tree task
		temp.addLine("getCharacterSource ",characterSourceTask);
		temp.addLine("setCharacter " + CharacterStates.toExternal(currentChar));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module that calculates numbers for characters with another character", "[name of module]", commandName, "setNumberTask")) {
			NumberFor2Characters temp =  (NumberFor2Characters)replaceEmployee(NumberFor2Characters.class, arguments, "Number for character with another character", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				parametersChanged();
				return numberTask;
			}
		}
		else if (checker.compare(this.getClass(), "Sets module supplying characters", "[name of module]", commandName, "setCharacterSource")) {//temporary
			return characterSourceTask.doCommand(commandName, arguments, checker);
		}
		else if (checker.compare(this.getClass(), "Returns module supplying characters", null, commandName, "getCharacterSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Goes to next character", null, commandName, "nextCharacter")) {
			if (currentChar>=characterSourceTask.getNumberOfCharacters(taxa)-1)
				currentChar=0;
			else
				currentChar++;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Goes to previous character", null, commandName, "previousCharacter")) {
			if (currentChar<=0)
				currentChar=characterSourceTask.getNumberOfCharacters(taxa)-1;
			else
				currentChar--;
			//charStates = null;
			parametersChanged(); //?
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseCharacter")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " to calculate value for tree ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentChar = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacter")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				currentChar = ic;
				//charStates = null;
			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				currentChar = ic;
				//charStates = null;
				parametersChanged(); //?
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(CharacterDistribution charStates){
		taxa = charStates.getTaxa();
		CharacterDistribution otherCharStates  = characterSourceTask.getCharacter(taxa, currentChar);
		numberTask.initialize(charStates, otherCharStates);
		if (taxa==null)
			taxa = getProject().chooseTaxa(containerOfModule(), "Taxa"); 
	}
	public void endJob(){
		if (taxa!=null)
			taxa.removeListener(this);
		super.endJob();
	}
	MesquiteString rs = new MesquiteString();
	/*.................................................................................................................*/
	public void calculateNumber(CharacterDistribution charStates, MesquiteNumber result, MesquiteString resultString) {
		if (result==null || charStates == null)
			return;
		clearResultAndLastResult(result);
		if (taxa==null){
			initialize(charStates);
		}
		CharacterDistribution otherCharStates = characterSourceTask.getCharacter(taxa, currentChar);
		rs.setValue("");
		numberTask.calculateNumber(charStates, otherCharStates, result, rs);
		if (resultString!=null) {
			resultString.setValue("For character " + (currentChar + 1) + ", ");
			resultString.append(rs.toString());
		}
		saveLastResult(result);
		saveLastResultString(resultString);
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee==characterSourceTask) {
			currentChar = 0;
			parametersChanged(notification);
		}
		else
			super.employeeParametersChanged(employee, source, notification);
	}
	/*.................................................................................................................*/
	public String getParameters(){
		return "Calculator: " + numberTask.getName() + " with character " + (currentChar+1); 
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Character value using other character";
	}
	/*.................................................................................................................*/
	public String getVeryShortName() {
		if (numberTask ==null)
			return "Character value using other character";
		else
			return numberTask.getVeryShortName();
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Character value using other character....";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the calculation of a number for a character based on another character (e.g., a correlation between the two characters)." ;
	}


}

