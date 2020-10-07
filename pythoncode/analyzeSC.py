#!/usr/bin/env python
import pdb,sys,os
import numpy as np
import math

def zscore(X):
	NX=[item for item in X if np.isnan(item)==False]
	mu=np.mean(NX)
	std=np.std(NX)
	X=[(item-mu)*1.0/std if item!=np.nan else item for item in X]
	return X
	
def avg(X,W):
	X=[X[k]*W[k] for k in range(len(X))]
	NX=[item for item in X if np.isnan(item)==False]
	return np.mean(NX)
	
def readJson(fpath):
	f=open(fpath,'r')
	lf=f.readlines()[0].strip()
	f.close()
	fjson=eval(lf[5:])
	return fjson 

def countCells(cnode,nodes):
	kids=cnode['children']
	ACell=[]
	if kids=='null':
		ACell+=cnode['CELL']
		return ACell
	else:
		ACell+=cnode['CELL']
		for j in kids:
			ACell+=countCells(nodes[j],nodes)
		return ACell

def parseJson(fjson,outf,distTfs,distThresh,fntf):
	nodes=fjson[3]
	edges=fjson[4]
	AllCells=[]
	NTFs=len(fjson[5])
	for i in nodes:
		AllCells+=i['CELL']
		i['ACell']=countCells(i,nodes)

	
	TF=[]
	pcount=1e-6
	
	
	
	
	# TFs
	for i in edges:
		ietf=i['etf']
		icells=nodes[i['to']]['ACell']
		perCell=len(icells)*1.0/len(AllCells)
		for j in ietf:
			rj=[j[1],-1*math.log(j[0]+pcount,10),j[2],perCell] if len(j)>2 else [j[1],-1*math.log(j[0]+pcount,10),np.nan,perCell]
			TF.append(rj)
	
	
	
	# eTFs
	for i in nodes:
		ietf=i['eTF']
		icells=i['ACell']
		perCell=len(icells)*1.0/len(AllCells)
		
		for j in ietf:
			rj=[j[1],-1*math.log(j[0]+pcount,10),np.nan,perCell]
			TF.append(rj)	
	
			
	TF_genes=[item[0] for item in TF]
	TF_scores=['p-value','reg_coeff','cell_percentage']
	TF_mat=[item[1:] for item in TF]
	
	
	W=[1,1,0.5] # Score weights
	TF_mat=np.array(TF_mat).T
	
	TF_mat=[zscore(item) for item in TF_mat]
	TF_mat=np.array(TF_mat).T
	TF_mat=[avg(item,W) for item in TF_mat]
	
	
	dTF={}
	for i in range(len(TF_genes)):
		gi=TF_genes[i]
		si=TF_mat[i]
		if gi not in dTF:
			dTF[gi]=si 
		else:
			dTF[gi]=max(si,dTF[gi])
	
	outTF=[]
	for i in dTF:
		outTF.append([i,dTF[i]])
	
	outTF=sorted(outTF,key=lambda x: x[1],reverse=True)
	
	
	SF=5.0
	for i in range(len(outTF)):
		pi=1-i*1.0/NTFs
		if pi>distThresh:
			outTF[i][1]=pi*SF
			
	with open(fntf,'r') as f:
		FR=f.readline().strip()
		FR=FR.split("\t")[1:]
		kTFs=FR
		
	outTF=[item for item in outTF if item[0] in kTFs]	
	outTF=outTF[:int(distTfs)]	
	outTF=[[str(k) for k in item] for item in outTF]
	outTF=["\t".join(item) for item in outTF]
	outTF="\n".join(outTF)
	
	
	with open(outf,'w') as f:
		f.write(outTF)

def main():	
	print("starting...")
	fnex=sys.argv[1]
	fntf=sys.argv[2]
	fnout=sys.argv[3]
	modelDir=sys.argv[4]
	distTfs=float(sys.argv[5])     # 50
	distThresh=float(sys.argv[6])  # 0.5

	tmpout="%s/tmp_out"%(modelDir)
	#pdb.set_trace()
	#print("cmd: python3 pythoncode/analyzeSC.py %s %s %s %s %s %s"%(fnex,fntf,fnout,modelDir,distTfs,distThresh));	
	print("calling scdiff2...")
	#os.system("python3 pythoncode/scdiff/scdiff/scdiff.py -i %s -t %s -k auto -l 1 -s 1 -o %s"%(fnex,fntf,tmpout))
	os.system("scdiff2 -i %s -o %s -t %s --maxloop 0 --ncores 10"%(fnex,tmpout,fntf))
	fjson=readJson("%s/InteractiveViz/%s.json"%(tmpout,fnex.split("/")[-1]))
	parseJson(fjson,fnout,distTfs,distThresh,fntf)



if __name__=="__main__":
	main()
