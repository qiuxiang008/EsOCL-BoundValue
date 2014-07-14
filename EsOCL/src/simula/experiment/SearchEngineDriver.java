package simula.experiment;

import java.io.*;

import simula.ocl.distance.DisplayResult;
import simula.ocl.distance.ValueElement4Search;
import simula.ocl.distance.SolveProblem;
import simula.oclga.*;
import simula.standalone.analysis.OCLExpUtility;
import simula.standalone.analysis.UMLModelInsGenerator;
import simula.standalone.modelinstance.UMLObjectIns;

import java.util.*;

public class SearchEngineDriver {

	String inputModelPath;

	String inputOclConstraintsPath;

	int searchAlgorithm = 0;

	public static int boundValueStratergy = 0;
	
	public static String filepath = "C:\\luhong\\data\\";
	public static FileWriter writer = null;

	Search[] s = new Search[] { new simula.oclga.AVM(),
			new simula.oclga.SSGA(100, 0.75), new simula.oclga.OpOEA(),
			new simula.oclga.RandomSearch() };

	String[] b = new String[] { "simple", "boundValue" };

	public SearchEngineDriver(String inputModelPath,
			String inputOclConstraintsPath, int searchAlgorithm,
			int boundValueStratergy) {
		this.inputModelPath = inputModelPath;
		this.inputOclConstraintsPath = inputOclConstraintsPath;
		this.searchAlgorithm = searchAlgorithm;
		this.boundValueStratergy = boundValueStratergy;
	}

	public static void main(String s[]) throws Exception {
//		String oclpath = "C:\\EsOCL\\resources\\constraints\\splitconstraint\\ocltest-";
//		String filepathbase = "C:\\data\\";
//		String modelpath = "C:\\EsOCL\\resources\\model\\Test.uml";
		String oclpath = "../resources/constraints/splitconstraint/ocltest-";
		String filepathbase = "C:\\data\\";
		String modelpath = "../resources/model/Test.uml";
//		System.out.println(oclpath);
//		System.out.println(modelpath);
//		
//		for(int j = 10; j <= 47; j++)
//		{   
			int j = 36;
			int splitkey = 2;
			
			oclpath = oclpath.concat(String.valueOf(j)).concat(" (").concat(String.valueOf(splitkey)).concat(").ocl");
//			for(int algokey = 0; algokey < 4; algokey++)
//			{
//				filepath = filepathbase.concat("Cisco_OCL_").concat(String.valueOf(j)).concat("_Alg_").concat(String.valueOf(algokey));
			filepath = filepathbase.concat("Cisco_OCL_").concat(String.valueOf(j)).concat("_SubOCL_").concat(String.valueOf(splitkey)).concat("_Alg_");
//				writer = new FileWriter(filepath);
				
//				for(int i = 0; i < 100; i++)
//				{
					SearchEngineDriver exp = new SearchEngineDriver(
							modelpath,
							oclpath, 0, 1);
					exp.runSearch();
					
//				}
//				writer.close();
//			}
			
		}
//	}

	/**
	 * for kunming
	 * 
	 * @param assgnedValue4Attribute
	 * @param OptimizedValueofAttributes
	 * @return
	 */
	public boolean getOptimizedValueofAttributes(
			ValueElement4Search[] assgnedValue4Attribute,
			ValueElement4Search[] OptimizedValueofAttributes) {
		boolean isSolved = false;
		String[] inputProfilePaths = {};
		SolveProblem xp = new SolveProblem(this.inputModelPath,
				inputProfilePaths, this.inputOclConstraintsPath);
		xp.processProblem(assgnedValue4Attribute, OptimizedValueofAttributes);
		if (this.boundValueStratergy == 0) {
			isSolved = searchProcess(xp);
			DisplayResult.resultList = new ArrayList<List<UMLObjectIns>>();
			DisplayResult.resultList.add(xp.getUmlModelInsGenerator()
					.getUmlObjectInsList());
		} else {
			int iterateTime = OCLExpUtility.INSTANCE.buildIndexArray4Bound(xp
					.getConstraint());
			DisplayResult.boundValueTypes = OCLExpUtility.INSTANCE
					.getTypeArray();
			DisplayResult.resultList = new ArrayList<List<UMLObjectIns>>();
			for (int i = 0; i < iterateTime; i++) {
				OCLExpUtility.INSTANCE.generateBoundValue(i);
				isSolved = searchProcess(xp);
				OCLExpUtility.INSTANCE.restoreOriginalValue();
				DisplayResult.resultList.add(xp.getUmlModelInsGenerator()
						.getUmlObjectInsList());
			}
		}
		xp.getAssignVlue();
		return isSolved;
	}

	public void runSearch() throws Exception {

		String[] inputProfilePaths = {};
		SolveProblem xp = new SolveProblem(this.inputModelPath,
				inputProfilePaths, this.inputOclConstraintsPath);
		xp.processProblem();
		if (this.boundValueStratergy == 0) {
			searchProcess(xp);
			// This class will store the final model instance
			DisplayResult.resultList = new ArrayList<List<UMLObjectIns>>();
			DisplayResult.resultList.add(xp.getUmlModelInsGenerator()
					.getUmlObjectInsList());
		} else {
			/**
			 * if we confirm the number of comparison expression, we can
			 * calculate the times for running the search process
			 */
			for(int algokey = 0; algokey < 4; algokey++)
			{
				this.searchAlgorithm = algokey;
				
				
				for(int j = 0; j < 10; j++)
				{
					String newpath = filepath.concat(String.valueOf(algokey)).concat("_Run_").concat(String.valueOf(j)).concat(".txt");
//					filepath = filepath.concat("_Run_").concat(String.valueOf(j)).concat(".txt");
					writer = new FileWriter(newpath);
					int iterateTime = OCLExpUtility.INSTANCE.buildIndexArray4Bound(xp
							.getConstraint());
					// it stores the type information of bound value for each comparison
					// expression
					DisplayResult.boundValueTypes = OCLExpUtility.INSTANCE
							.getTypeArray();
					DisplayResult.resultList = new ArrayList<List<UMLObjectIns>>();
					for (int i = 0; i < iterateTime; i++) {
						// modify the right part value of comparison expression
						OCLExpUtility.INSTANCE.generateBoundValue(i);
						searchProcess(xp);
						// restore the right part value of comparison expression
						OCLExpUtility.INSTANCE.restoreOriginalValue();
						DisplayResult.resultList.add(xp.getUmlModelInsGenerator()
								.getUmlObjectInsList());
					}
					writer.close();
				}
			}
			
			

		}

	}

	public boolean searchProcess(SolveProblem xp) {
		Search sv = s[this.searchAlgorithm];
		sv.setMaxIterations(2000);
		Search.validateConstraints(xp);
		String[] value = sv.search(xp);
		// for (String string : value) {
		// System.out.print(string+" ");
		// }
		// System.out.println();
		boolean found = xp.getFitness(value) == 0d;
		int steps = sv.getIteration();
		try {
			writer.write(steps + "\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sv.getShortName());
		
		System.out.println(found);
		System.out.println(steps);
		return found;
	}
}
