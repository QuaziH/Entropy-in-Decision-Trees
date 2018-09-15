import java.awt.*;
import java.util.*;

public class DecisionTreeClass{
	private static class DecisionTreeNode implements Comparable<DecisionTreeNode>{
		public ArrayList<Integer> data_list; // list of data IDs
		public int opt_fea_type = -1;	// 0 if continuous, 1 if categorical
		public int opt_fea_id = -1;		// the index of the optimal feature
		public double opt_fea_thd = Double.NEGATIVE_INFINITY;	// the optimal splitting threshold 
																// for continuous feature
		public int opt_improvement = Integer.MIN_VALUE; // the improvement if split based on the optimal feature
		public boolean is_leaf = true;		// is it a leaf
		public int majority_class = -1;		// class prediction based on majority vote
		public int num_accurate = -1;		// number of accurate data using majority_class
		public DecisionTreeNode parent = null;		// parent node
		public ArrayList<DecisionTreeNode> children = new ArrayList<>(); 	// list of children when split
		
		public DecisionTreeNode(ArrayList<Integer> d_list, int m_class, int n_acc){
			data_list = new ArrayList<Integer>(d_list);
			majority_class = m_class;
			num_accurate = n_acc;
		}

		@Override
		public int compareTo(DecisionTreeNode o) {
			return Integer.compare(this.opt_improvement, o.opt_improvement);
		}
	}
	
	public DataWrapperClass train_data;
	public int max_height;
	public int max_num_leaves;
	public int height;
	public int num_leaves;
	public DecisionTreeNode root;
	
	// constructor, build the decision tree using train_data, max_height and max_num_leaves
	public DecisionTreeClass(DataWrapperClass t_d, int m_h, int m_n_l){
		train_data = t_d;
		max_height = m_h;
		max_num_leaves = m_n_l;

		PriorityQueue<DecisionTreeNode> maxHeap = new PriorityQueue<>(30, Comparator.reverseOrder());

		int mc, na;
		ArrayList<Integer> used = new ArrayList<>();

		ArrayList<Integer> list = new ArrayList<>();
		for(int i = 0; i < train_data.labels.size(); i++){
			list.add(i);
		}

		int[] maj_class_label = majority_class(train_data.labels);
		mc = maj_class_label[0];
		na = maj_class_label[1];

		root = new DecisionTreeNode(list, mc, na);

		height = 0;
		num_leaves = 1;

		if(na == train_data.labels.size()) {
			root.opt_improvement = 0;
		} else {
			if(train_data.categorical_features != null){
				root.opt_fea_type = 1;

				ArrayList<Integer> mc_na_index_list = find_optimal_feature_for_first(train_data, train_data.categorical_features);
				root.opt_fea_id = mc_na_index_list.get(mc_na_index_list.size() - 1);

				ArrayList<ArrayList<Integer>> data = sort_data(train_data.categorical_features, train_data.num_diff_data, root.opt_fea_id);
				used.add(root.opt_fea_id);

				for(int i = 0, j = 0, k = 1; i < data.size(); i++, j+=2, k+=2){
					if(data.get(i).size() == 0)
						continue;
					DecisionTreeNode node = new DecisionTreeNode(data.get(i), mc_na_index_list.get(j), mc_na_index_list.get(k));
					node.opt_fea_type = 1;
					node.opt_fea_id = compare_opt_feature(train_data, data, used, i);

					node.opt_improvement = check_for_improvement(train_data, data, used, i, mc_na_index_list.get(k));
					root.is_leaf = false;
					node.parent = root;
					root.children.add(node);
					maxHeap.add(node);
				}

				height++;
				num_leaves += train_data.num_diff_data - 1;

				while(max_height != height && max_num_leaves != num_leaves) {
					DecisionTreeNode temp = maxHeap.poll();

					data = sort_by_feature(train_data, temp.data_list, temp.opt_fea_id);
					used.add(temp.opt_fea_id);

					for(int i = 0, j = 0, k = 1; i < data.size(); i++){
						int[] mc_na_holder = majority_class(data.get(i));
						if(data.get(i).size() == 0)
							continue;
						DecisionTreeNode node = new DecisionTreeNode(data.get(i), mc_na_holder[j], mc_na_holder[k]);
						node.opt_fea_type = 1;
						node.opt_fea_id = compare_opt_feature(train_data, data, used, i);

						node.opt_improvement = check_for_improvement(train_data, data, used, i, mc_na_holder[k]);
						temp.is_leaf = false;
						node.parent = temp;
						temp.children.add(node);
						maxHeap.add(node);
					}

					num_leaves += train_data.num_diff_data - 1;
					height++;

					if(maxHeap.peek().opt_improvement == 0)
						break;
				}

//				int sum = 0;
//				while(!maxHeap.isEmpty()) {
//					sum += maxHeap.poll().num_accurate;
//				}
//				System.out.println(sum);

			}
			if (train_data.continuous_features != null){
				root.opt_fea_type = 0;

				for(int i = 0; i < train_data.num_features; i++){

				}
			}
		}
	}
	
	
	public ArrayList<Integer> predict(DataWrapperClass test_data){
		ArrayList<Integer> prediction = new ArrayList<>();
		ArrayList<ArrayList<Integer>> data = new ArrayList<>();
		DecisionTreeNode temp;
		temp = root;
		data = sort_data(test_data.categorical_features, test_data.num_diff_data, temp.opt_fea_id);
		temp = temp.children.get(temp.children.size()-1);
		while(!temp.is_leaf){
			data = sort_new_data(test_data, data, data.size()-1, temp.opt_fea_id);
			if(temp.children.size() == 0)
				break;
			temp = temp.children.get(temp.children.size()-1);
		}
		prediction = data.get(data.size()-1);

		ArrayList<Integer> maj = new ArrayList<>();
		for (int i : prediction)
			maj.add(test_data.labels.get(i));

		int majority =  majority_class(maj)[0];
		ArrayList<Integer> newLabel = new ArrayList<>();
		ArrayList<Integer> newPrediction = new ArrayList<>();
		for(int i = 0; i < prediction.size(); i++){
			newLabel.add(majority);
			newPrediction.add(test_data.categorical_features.get(prediction.get(i)).get(temp.opt_fea_id));
		}

		test_data.labels = newLabel;
		return newPrediction;
	}

	public int[] majority_class(ArrayList<Integer> data){
		int mc = 0, na = 0;
		int[] arr = new int[2];

		Map<Integer, Integer> majorityClass = new HashMap<>();

		for(int i : data) {
			if (majorityClass.get(i) == null) {
				majorityClass.put(i, 1);
			} else {
				majorityClass.replace(i, majorityClass.get(i) + 1);
			}
		}

		for(Map.Entry entry : majorityClass.entrySet()) {
			if((int)entry.getValue() > na){
				mc = (int)entry.getKey();
				na = (int)entry.getValue();
			}
		}

		arr[0] = mc;
		arr[1] = na;
		return arr;
	}

	public ArrayList<ArrayList<Integer>> sort_data(ArrayList<ArrayList<Integer>> data, int size, int col){
		ArrayList<ArrayList<Integer>> sorted_data = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < size; i++){
			ArrayList<Integer> newList = new ArrayList<>();
			sorted_data.add(newList);
		}

		for(int i = 0; i < data.size(); i++){
			sorted_data.get(data.get(i).get(col)).add(i);
		}

		sorted_data.removeIf(ArrayList::isEmpty);

		return sorted_data;
	}

	public ArrayList<Integer> find_optimal_feature_for_first(DataWrapperClass data, ArrayList<ArrayList<Integer>> feature){
		ArrayList<ArrayList<Integer>> list;
		int[] accurate = new int[data.num_features];
		ArrayList<ArrayList<Integer>> holder = new ArrayList<>();

		for(int i = 0; i < data.num_features; i++){
			list = sort_data(feature, data.num_diff_data, i);
			ArrayList<Integer> second_holder = new ArrayList<>();

			int na = 0;
			for (ArrayList<Integer> batch : list) {
				ArrayList<Integer> temp = new ArrayList<>();
				for (Integer feature_index : batch)
					temp.add(data.labels.get(feature_index));
				int[] mc_na_holder = majority_class(temp);
				second_holder.add(mc_na_holder[0]);
				second_holder.add(mc_na_holder[1]);
				na += mc_na_holder[1];
//				System.out.print(mc_na_holder[1] + " ");
			}
			holder.add(second_holder);
//			System.out.println();
			accurate[i] = na;
		}

//		for(int i = 0; i < accurate.length; i++)
//			System.out.print(accurate[i] + " ");
//		System.out.println();

		int index = -1;
		int max = 0;

		for (int i = 0; i < accurate.length; i++) {
			if (accurate[i] > max) {
				max = accurate[i];
				index = i;
			}
		}

		holder.get(index).add(index);
		return holder.get(index);
	}

	public ArrayList<ArrayList<Integer>> sort_new_data(DataWrapperClass data_list, ArrayList<ArrayList<Integer>> feature, int row, int col){
		ArrayList<ArrayList<Integer>> sorted_data = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < data_list.num_diff_data; i++){
			ArrayList<Integer> newList = new ArrayList<>();
			sorted_data.add(newList);
		}

		for(int i = 0; i < feature.get(row).size(); i++){
			if(data_list.categorical_features != null) {
				int index = data_list.categorical_features.get(feature.get(row).get(i)).get(col);
				sorted_data.get(index).add(i);
			}
			else if (data_list.continuous_features != null)
				sorted_data.get(feature.get(row).get(i)).add(i); //needs testing
		}

		sorted_data.removeIf(ArrayList::isEmpty);

		return sorted_data;
	}

	public ArrayList<ArrayList<Integer>> sort_by_feature(DataWrapperClass data_list, ArrayList<Integer> feature, int col){
		ArrayList<ArrayList<Integer>> sorted_data = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < data_list.num_diff_data; i++){
			ArrayList<Integer> newList = new ArrayList<>();
			sorted_data.add(newList);
		}

		for(int i = 0; i < feature.size(); i++){
			if(data_list.categorical_features != null) {
				int index = data_list.categorical_features.get(feature.get(i)).get(col);
				sorted_data.get(index).add(i);
			} else if (data_list.continuous_features != null) {
//				sorted_data.get(feature.get(row).get(i)).add(i); //needs testing
			}
		}

		sorted_data.removeIf(ArrayList::isEmpty);

		return sorted_data;
	}

	public int compare_opt_feature(DataWrapperClass data, ArrayList<ArrayList<Integer>> feature, ArrayList<Integer> used, int row){
		ArrayList<ArrayList<Integer>> list;
		int features_used_index = 0;
		int[] accurate = new int[data.num_features];
		Collections.sort(used);

		for(int i = 0; i < data.num_features; i++){
			if (features_used_index < used.size() && i == used.get(features_used_index)) {
				features_used_index++;
				accurate[i] = -1;
				continue;
			}

			list = sort_new_data(data, feature, row, i);

			int na = 0;
			for (ArrayList<Integer> batch : list) {
				ArrayList<Integer> temp = new ArrayList<>();
				for (Integer feature_index : batch)
					temp.add(data.labels.get(feature_index));
				int[] mc_na_holder = majority_class(temp);
				na += mc_na_holder[1];
			}
			accurate[i] = na;
		}

//		for(int i = 0; i < accurate.length; i++)
//			System.out.print(accurate[i] + " ");
//		System.out.println();

		int index = -1;
		int max = 0;

		for (int i = 0; i < accurate.length; i++) {
			if (accurate[i] > max) {
				max = accurate[i];
				index = i;
			}
		}
		return index;
	}

	public int check_for_improvement(DataWrapperClass data, ArrayList<ArrayList<Integer>> feature, ArrayList<Integer> used, int row, int prev_improvement){
		ArrayList<ArrayList<Integer>> list;
		int features_used_index = 0;
		int[] accurate = new int[data.num_features];
		Collections.sort(used);

		for(int i = 0; i < data.num_features; i++){
			if (features_used_index < used.size() && i == used.get(features_used_index)) {
				features_used_index++;
				accurate[i] = -1;
				continue;
			}

			list = sort_new_data(data, feature, row, i);

			int na = 0;
			for (ArrayList<Integer> batch : list) {
				ArrayList<Integer> temp = new ArrayList<>();
				for (Integer feature_index : batch)
					temp.add(data.labels.get(feature_index));
				int[] mc_na_holder = majority_class(temp);
				na += mc_na_holder[1];
			}
			accurate[i] = na;
		}

//		for(int i = 0; i < accurate.length; i++)
//			System.out.print(accurate[i] + " ");
//		System.out.println();

		int index = -1;
		int max = 0;

		for (int i = 0; i < accurate.length; i++) {
			if (accurate[i] > max) {
				max = accurate[i];
				index = i;
			}
		}

		return max-prev_improvement;
	}
}
