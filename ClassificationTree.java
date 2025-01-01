// Krish Doshi
// 11/20/2024
// CSE 123 
// P3: Spam Classifier
// TA: Cynthia

import java.util.*;
import java.io.*;

//Represents a Classification Tree model that predicts a label for input data
//provided by the client
public class ClassificationTree extends Classifier {
    private static final int SPACING_TO_VALUE = 2;
    private ClassificationNode overallRoot;

    // Behavior: 
    //          - creates a classification tree based on data loaded from a file provided by
    //          - the client (the input fild must be in pre-order format)
    // Parameters:
    //          - 'sc': provides the input from the given file (assumed to be non-null)
    public ClassificationTree(Scanner sc){
        this.overallRoot = makeTree(sc);
    }

    // Behavior: 
    //          - creates a classification tree based on data loaded from a file provided by
    //          - the client. This is done by checking each line of the file for a label or split 
    //          - (threshold and feature) and adding each to the classfication tree.
    // Parameters:
    //          - 'sc': provides the input from the given file (assumed to be non-null)
    // Returns: 
    //          - 'ClassificationNode': the completed classification tree that corresponds to the 
    //                                  data from the provided file
    private ClassificationNode makeTree(Scanner sc) {
        if (!sc.hasNextLine() || sc == null) {
            return null;
        } else {
            String firstLine = sc.nextLine();

            if (firstLine.contains("Feature")) {
                String threshold = sc.nextLine();

                ClassificationNode node =  new ClassificationNode(new Split(firstLine.substring(
                    firstLine.indexOf(":") + SPACING_TO_VALUE), 
                        Double.parseDouble(threshold.substring(threshold.indexOf(":") + 
                            SPACING_TO_VALUE))), makeTree(sc), makeTree(sc));


                return node;
                        
            } else {
                ClassificationNode node =  new ClassificationNode(firstLine);
                return node;
            }
        }
    }


    // Behavior: 
    //          - creates a classification tree from scratch based on the inputted data and 
    //          - corresponding labels provided. Splits are created in order to make sure that
    //          - the model predicts the correct label associated with the inputted data provided
    // Exceptions:
    //          - Throws an IllegalArgumentException if the provided lists of the data and the
    //          - labels aren't the same size or if they are empty
    // Parameters:
    //          - 'data': a list of inputted data (assumed to be non-null)
    //          - 'results': a list of all the labels that corresponds to list of inputted data 
    //                       (assumed to be non-null)
    public ClassificationTree(List<Classifiable> data, List<String> results){
        if (data.size() != results.size() || data.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.overallRoot = new ClassificationNode(data.get(0), results.get(0));

        for (int i = 1; i < data.size(); i++) {
            this.overallRoot = makeTree(data, results, overallRoot, i);
        }

        
    }

    // Behavior: 
    //          - creates a classification tree from scratch based on the inputted data and 
    //          - corresponding labels provided. Splits are created in order to make sure that
    //          - the model predicts the correct label associated with the inputted data provided
    // Parameters:
    //          - 'data': a list of inputted data (assumed to be non-null)
    //          - 'results': a list of all the labels that corresponds to list of inputted data 
    //                       (assumed to be non-null)
    //          - 'curr': current location within the classification tree
    //          - 'index': current location within the list of inputted data and the 
    //                     corresponding list of labels
    // Returns: 
    //          - 'ClassificationNode': the completed classification tree that corresponds to the 
    //                                  inputted data and the provided labels
    private ClassificationNode makeTree(List<Classifiable> data, List<String> results, 
            ClassificationNode curr, int index){

        if (curr.split != null) {
            if (curr.split.evaluate(data.get(index))) {
                curr.left = makeTree(data, results, curr.left, index);
            } else {
                curr.right = makeTree(data, results, curr.right, index);
            }

        } else {
            if (!curr.label.equals(results.get(index))) {
                ClassificationNode intermediateNode = 
                        new ClassificationNode(data.get(index).partition(curr.inputData));

                ClassificationNode newLabel = 
                        new ClassificationNode(data.get(index), results.get(index));

                boolean split = intermediateNode.split.evaluate(data.get(index));

                intermediateNode.left =  split ? newLabel: curr;
                intermediateNode.right = split ? curr: newLabel;

                return intermediateNode;
            }

        }

        return curr;
    }



    // Behavior: 
    //          - checks whether the input given has all the features that are present within
    //          - the classification tree. If it doesn't have all of the features, then it can't
    //          - be classified by the classification tree
    // Parameters:
    //          - 'input': input being given to see if it can be classified
    //                     by the model (assumed to be non-null)
    // Returns: 
    //          - 'boolean': whether or not the input can be classified by the model (true if
    ///                      it can be and false if it can't)
    @Override
    public boolean canClassify(Classifiable input) {
        return canClassify(input, overallRoot);
    }


    // Behavior: 
    //          - checks whether the input given has all the features that are present within
    //          - the classification tree. If it doesn't have all of the features, then it can't
    //          - be classified by the classification tree
    // Parameters:
    //          - 'input': input being given to see if it can be classified
    //                     by the model (assumed to be non-null)
    //          - 'curr': current location within the classification tree
    // Returns: 
    //          - 'boolean': whether or not the input can be classified by the model (true if
    ///                      it can be and false if it can't)
    private boolean canClassify(Classifiable input, ClassificationNode curr){
        if (curr != null && curr.split != null) {
            return input.getFeatures().contains(curr.split.getFeature()) && 
                    canClassify(input, curr.left) && canClassify(input, curr.right);
        }

        return true;
    }


    // Behavior: 
    //          - Classifies the inputted data and provides the associated label to
    //          - the data based on the model
    // Exceptions:
    //          - Throws an IllegalArgumentException if the input can't be classified by the 
    //          - model
    // Parameters:
    //          - 'input': inputted data that will be classified by the model (assumed to be
    //                     non-null)
    // Returns: 
    //          - 'String': label provided by the model that corresponds to the provided input
    @Override
    public String classify(Classifiable input) {
        if (!canClassify(input)) {
            throw new IllegalArgumentException();
        }

        return classify(input, overallRoot);
    }


    // Behavior: 
    //          - Classifies the inputted data and provides the associated label to
    //          - the data based on the model
    // Parameters:
    //          - 'input': inputted data that will be classified by the model (assumed to be 
    //                     non-null)
    //          - 'curr': current location within the classification tree
    // Returns: 
    //          - 'String': label provided by the model that corresponds to the provided input
    private String classify(Classifiable input, ClassificationNode curr) {
        if (curr != null && curr.split != null) {
            if (curr.split.evaluate(input)) {
                return classify(input, curr.left);
            } else {
                return classify(input, curr.right);
            }
        }

        return curr.label;
    }

    
    // Behavior: 
    //          - saves the classification tree model to a file with splits listing the features
    //          - and thresholds and the labels listing the name of the label (the output file 
    //          - will be in pre-order format)
    // Parameters:
    //          - 'ps': writes the classfication tree to a file (assumed to be non-null)
    @Override
    public void save(PrintStream ps) {
        save(ps, overallRoot);
    }

    // Behavior: 
    //          - saves the classification tree model to a file with splits listing the features
    //          - and thresholds and the labels listing the name of the label.
    // Parameters:
    //          - 'ps': writes the classfication tree to a file (assumed to be non-null)
    //          - 'curr': current location within the classification tree
    private void save(PrintStream ps, ClassificationNode curr){
        if (curr != null) {
            if (curr.label == null) {
                ps.println(curr.split.toString());
            } else {
                ps.println(curr.label);
            }

            save(ps, curr.left);
            save(ps, curr.right);
        }
    }

    //Represents either a split or a label within a classification tree model
    private static class ClassificationNode{
        public ClassificationNode left;
        public ClassificationNode right;
        public Classifiable inputData;
        public String label;
        public Split split;

        // Behavior: 
        //          - creates a label for the classification tree that also stores the
        //          - corresponding input data to that label
        // Parameters:
        //          - 'label': a label being added to the classification tree
        //          - 'inputData': input data corresponding to the label
        public ClassificationNode(Classifiable inputData, String label){
            this(label);
            this.inputData = inputData;
        }

        // Behavior: 
        //          - creates a split for the classification tree 
        // Parameters:
        //          - 'split': a split being added to the classification tree
        public ClassificationNode(Split split){
            this.split = split;
        }

        // Behavior: 
        //          - creates a split for the classification tree and defines the left and
        //          - right 
        // Parameters:
        //          - 'split': a split being added to the classification tree
        public ClassificationNode(Split split, ClassificationNode left, ClassificationNode right){
            this.split = split;
            this.left = left;
            this.right = right;
        }

        // Behavior: 
        //          - creates a label for the classification tree 
        // Parameters:
        //          - 'label': a label being added to the classification tree
        public ClassificationNode(String label){
            this.label = label;
        }  
    }
    
}
