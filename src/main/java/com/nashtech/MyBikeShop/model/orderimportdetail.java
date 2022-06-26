package com.nashtech.MyBikeShop.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nashtech.MyBikeShop.DTO.OrderDetailDTO;

@Entity
@Table(name = "orderimportdetails")
public class orderimportdetail {
	@EmbeddedId
	private OrderImportDetailsKey id;

	@Column(name = "amount")
	private int ammount;
	
	@Column(name = "price")
	private Float price;

	@MapsId("orderId")
	@ManyToOne
	@JoinColumn(name = "orderimportid", insertable = false, updatable = false)
	@JsonBackReference
	private orderimport order;

	@MapsId("productId")
	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "productid", insertable = false, updatable = false)
	private com.nashtech.MyBikeShop.model.product product;

	@Embeddable
	public static class OrderImportDetailsKey implements Serializable {

		private static final long serialVersionUID = -4415409401138657585L;

		@Column(name = "orderimportid", nullable = false, updatable = false)
		private int orderId;

		@Column(name = "productid", nullable = false, updatable = false)
		private String productId;

		public OrderImportDetailsKey() {
			super();
		}

		public int getOrderId() {
			return orderId;
		}

		public void setOrderId(int orderId) {
			this.orderId = orderId;
		}

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

		public OrderImportDetailsKey(int orderId, String productId) {
			super();
			this.orderId = orderId;
			this.productId = productId;
		}

		@Override
		public int hashCode() {
			return Objects.hash(orderId, productId);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OrderImportDetailsKey other = (OrderImportDetailsKey) obj;
			return orderId == other.orderId && Objects.equals(productId, other.productId);
		}

	}

	public orderimportdetail() {
	}

	public orderimportdetail(OrderImportDetailsKey id, int ammount, Float price) {
		super();
		this.id = id;
		this.ammount = ammount;
		this.price = price;
	}

	public orderimportdetail(OrderImportDetailsKey id, int ammount, Float price, orderimport order,
							 com.nashtech.MyBikeShop.model.product product) {
		super();
		this.id = id;
		this.ammount = ammount;
		this.price = price;
		this.order = order;
		this.product = product;
	}



	public orderimportdetail(OrderDetailDTO orderDTO) {
		super();
		this.ammount = orderDTO.getAmmount();
	}


	public OrderImportDetailsKey getId() {
		return id;
	}



	public void setId(OrderImportDetailsKey id) {
		this.id = id;
	}



	public Float getPrice() {
		return price;
	}



	public void setPrice(Float price) {
		this.price = price;
	}



	public orderimport getOrder() {
		return order;
	}



	public void setOrder(orderimport order) {
		this.order = order;
	}



	public com.nashtech.MyBikeShop.model.product getProduct() {
		return product;
	}

	public void setProduct(com.nashtech.MyBikeShop.model.product product) {
		this.product = product;
	}

	public int getAmmount() {
		return ammount;
	}

	public void setAmmount(int ammount) {
		this.ammount = ammount;
	}

	@Override
	public String toString() {
		return "OrderImportDetailEntity [id=" + id + ", ammount=" + ammount + ", price=" + price + ", order=" + order
				+ ", product=" + product + "]";
	}

}
