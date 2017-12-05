package com.hypersocket.email;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="email_batch_items")
public class EmailBatchItem extends RealmResource {

	private static final long serialVersionUID = 7712547601212721547L;

}
