package pl.allegro.promo.devoxx2015.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.devoxx2015.domain.Offer;
import pl.allegro.promo.devoxx2015.domain.OfferRepository;
import pl.allegro.promo.devoxx2015.domain.PhotoScoreSource;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OfferService {

    private final OfferRepository offerRepository;
    private final PhotoScoreSource photoScoreSource;

    @Autowired
    public OfferService(OfferRepository offerRepository, PhotoScoreSource photoScoreSource) {
        this.offerRepository = offerRepository;
        this.photoScoreSource = photoScoreSource;
    }

    public void processOffers(List<OfferPublishedEvent> events) {
        events.forEach(offerPublishedEvent -> {
            double score;
            try {
                score = photoScoreSource.getScore(offerPublishedEvent.getPhotoUrl());
            } catch (Exception e) {
                score = 0.7;
            }

            if (score >= 0.7) {
                Offer offer = new Offer(offerPublishedEvent.getId(), offerPublishedEvent.getTitle(), offerPublishedEvent.getPhotoUrl(), score);
                offerRepository.save(offer);
            }
        });
    }

    public List<Offer> getOffers() {
        return offerRepository.findAll()
                .stream()
                .sorted((o1, o2) -> Double.compare(o2.getPhotoScore(), o1.getPhotoScore()))
                .collect(Collectors.toList());
    }
}
