package SeeDev.DataStructure;

public class EntityGroups {
	
	/*
	DNA_Product =    RNA|Protein|Protein_Family|Protein_Complex|Protein_Domain
	DNA =    Gene|Gene_Family|Box,Promoter
	Functional_Molecule =    DNA_Product|Hormone
	Molecule =    DNA|Functional_Molecule
	Dynamic_Process =    Regulatory_Network|Pathway
	Internal_Factor =    Tissue|Development_Phase|Genotype
	Factor = Internal_Factor|Environmental_Factor
	*/
	public static enum DNA_Product {RNA, Protein, Protein_Family, Protein_Complex, Protein_Domain}
	public static enum DNA {Gene, Gene_Family, Box, Promoter}
	public static enum Functional_Molecule {RNA, Protein, Protein_Family, Protein_Complex, Protein_Domain, Hormone}
	public static enum Molecule {Gene, Gene_Family, Box, Promoter, RNA, Protein, Protein_Family, Protein_Complex, Protein_Domain, Hormone}
	public static enum Dynamic_Process {Regulatory_Network, Pathway}
	public static enum Internal_Factor {Tissue,Development_Phase,Genotype}
	public static enum Factor {Tissue, Development_Phase, Genotype, Environmental_Factor}
	
}