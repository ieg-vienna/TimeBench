package org.timebench.R.data;

public class ModelInformationCriteria {
	private double aic, aicc, bic, loglik, sigma2;
	private int col;

	public ModelInformationCriteria(double aic, double aicc, double bic,
			double loglik, double sigma2) {
		this.aic = aic;
		this.aicc = aicc;
		this.bic = bic;
		this.loglik = loglik;
		this.sigma2 = sigma2;
	}
	
	public ModelInformationCriteria() {
	}

	public double getAic() {
		return aic;
	}

	public void setAic(double aic) {
		this.aic = aic;
	}

	public double getAicc() {
		return aicc;
	}

	public void setAicc(double aicc) {
		this.aicc = aicc;
	}

	public double getBic() {
		return bic;
	}

	public void setBic(double bic) {
		this.bic = bic;
	}

	public double getLoglik() {
		return loglik;
	}

	public void setLoglik(double loglik) {
		this.loglik = loglik;
	}

	public double getSigma2() {
		return sigma2;
	}

	public void setSigma2(double sigma2) {
		this.sigma2 = sigma2;
	}
	

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	@Override
	public String toString() {
		return "ModelInformationCriteria [aic=" + aic + ", aicc=" + aicc
				+ ", bic=" + bic + ", loglik=" + loglik + ", sigma2=" + sigma2
				+ "]";
	}
	
	
}
