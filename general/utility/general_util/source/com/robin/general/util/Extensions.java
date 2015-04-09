package com.robin.general.util;

import java.util.Optional;

public class Extensions {
	public static <T> T Coalesce(T obj1, T obj2){
		T result = obj1;
		if(result == null) result = obj2;
		return result;
	}
	
	public static <T> Optional<T> Coalesce(Optional<T>o1, Optional<T>o2){
		Optional<T>result = o1;
		if(!result.isPresent()) result = o2;
		return result;
	}
}
