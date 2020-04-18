package com.microservice.moviecatalogservice.services;

import com.microservice.moviecatalogservice.models.CatalogItem;
import com.microservice.moviecatalogservice.models.Movie;
import com.microservice.moviecatalogservice.models.Rating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Service
public class MovieInfo {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallBackCatalogItem",
            //BulkHead pattern
            threadPoolKey = "movieInfoPool",
            threadPoolProperties = {
            //Maximo de threads esperando
                @HystrixProperty(name = "coreSize", value = "20"),
                    //Máximo de registro na fila pra ser executado após as thread estarem liberadas.
                @HystrixProperty(name = "maxQueueSize", value = "10"),
            }
    )
    public CatalogItem getCatalogItem(Rating rating) {
        //WebCliente
        //Movie movie = webClientBuilder.build().get().uri("http://movie-info-service/movies/" + rating.getMovieId()).retrieve().bodyToMono(Movie.class).block();
        //For each movid ID, call movie info service and get Details
        Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
        return new CatalogItem(movie.getName(), "Desc", rating.getRating());
    }
    public CatalogItem getFallBackCatalogItem(Rating rating) {
        return new CatalogItem("Movie name not found", "", rating.getRating());
    }
}
