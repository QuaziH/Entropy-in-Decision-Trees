import java.util.*;
import java.io.File;

public class DataWrapperClass {
	public int num_data;		// number of data (N) every single number
	public int num_features;	// number of features (D) every column
	public int num_classes;		// number of different classes (K) how many diff label numbers
	public int num_diff_data;
	public int num_cont_fea; 	// number of continuous features
	public int num_cat_fea;		// number of categorical features
	public ArrayList<ArrayList<Double> > continuous_features;	// only continuous features
	public ArrayList<ArrayList<Integer> > categorical_features;	// only categorical features
	public ArrayList<Integer> labels;	// labels of all data
	
	// read feature and label file
	// store feature in continuous_/categorical_features, 
	// store labels 
	// if file name starts with 'CAT_', all features are categorical
	// otherwise, all features are continuous
	public DataWrapperClass(String feature_fname, String label_fname){
		Scanner scFeature = null;
		Scanner scLabel = null;
		try{
			scFeature = new Scanner(new File(feature_fname));
			scLabel = new Scanner(new File(label_fname));
			Set<Integer> hs;
			Set<Integer> different_data = new HashSet<>();
			num_data = 0;

			if(feature_fname.contains("CAT_")){
				categorical_features = new ArrayList<>();
				labels = new ArrayList<>();
				while(scFeature.hasNextLine()){
					ArrayList<Integer> categorical_person = new ArrayList<Integer>();
					String[] lineSplit = scFeature.nextLine().split("\\s+|\\t+");

					num_data++;
					for (String ls : lineSplit) {
						categorical_person.add(Integer.parseInt(ls));
						different_data.add(Integer.parseInt(ls));
					}
					labels.add(scLabel.nextInt());

					num_features = categorical_person.size();
					num_cat_fea = categorical_person.size();
					categorical_features.add(categorical_person);
				}
//				for(int j = 0; j < num_features; j++) {
//					for (int i = 0; i < categorical_features.size(); i++) {
//						different_data = new HashSet<>(categorical_features.get(i).get(j));
//					}
//					num_diff_data.add(different_data.size());
//				}
//				different_data = new HashSet<>(categorical_features.get(0));
				num_diff_data = different_data.size();

				hs = new HashSet<>(labels);
				num_classes = hs.size();
			} else {
				continuous_features = new ArrayList<>();
				labels = new ArrayList<>();
				while(scFeature.hasNextLine()){
					ArrayList<Double> continuous_person = new ArrayList<Double>();
					String[] lineSplit = scFeature.nextLine().replaceAll("^\\s+|\\s+$", "").split("\\s+|\\t+|\\n+|\\r+|\\f+|\\x0B+");

					num_data++;
					for (String ls : lineSplit) {
						continuous_person.add(Double.parseDouble(ls));
					}
					labels.add(scLabel.nextInt());

					num_features = continuous_person.size();
					num_cont_fea = continuous_person.size();
					continuous_features.add(continuous_person);
				}
				hs = new HashSet<>(labels);
				num_classes = hs.size();
			}

//			System.out.println(num_classes);
//			System.out.println(categorical_features);
//			System.out.println(continuous_features);

			scFeature.close();
			scLabel.close();
		} catch (Exception e){
			System.out.println("There was an error: " + e);
		}
	}
	
	// static function, compare two label lists, report how many are correct
	public static int evaluate(ArrayList<Integer> l1, ArrayList<Integer> l2){
		int len = l1.size();
		assert len == l2.size();	// length should be equal
		assert len > 0;				// length should be bigger than zero
		int ct = 0;
		for(int i = 0; i < len; ++i){
			if(l1.get(i).equals(l2.get(i))) ++ct;
		}
		return ct;
	}
	
	// static function, compare two label lists, report score (between 0 and 1)
	public static double accuracy(ArrayList<Integer> l1, ArrayList<Integer> l2){
		int len = l1.size();
		assert len == l2.size();	// label lists should have equal length
		assert len > 0;				// lists should be non-empty
		double score = evaluate(l1,l2);
		score = score / len;		// normalize by divided by the length
		return score;
	}
}
