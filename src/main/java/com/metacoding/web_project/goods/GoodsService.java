package com.metacoding.web_project.goods;

import com.metacoding.web_project._core.error.ex.Exception404;
import com.metacoding.web_project._core.util.PageUtil;
import com.metacoding.web_project.bid.Bid;
import com.metacoding.web_project.bid.BidRepository;
import com.metacoding.web_project.category.CategoryRepository;
import com.metacoding.web_project.user.User;
import com.metacoding.web_project.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.metacoding.web_project._core.util.FormatDate.formatRemainingTime;

@RequiredArgsConstructor
@Service
public class GoodsService {
    private final GoodsRepository goodsRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    // 제품 상세페이지 데이터 끌어오기
    @Transactional
    public GoodsResponse.GoodsDetailDTO getGoodsInfo(Integer id) {

        // 1. id에 해당하는 물품 데이터 가져오기
        Goods goods = goodsRepository.findById(id)
                .orElseThrow(() -> new Exception404("해당 물품은 존재하지 않습니다."));

        Integer tryPrice = 0;

        // 2. 해당 물품의 status가 0 이면 bid_tb에 데이터 가져오기
        if (goods.getStatus() == 0) {
            Optional<Bid> result = bidRepository.findByGoodsDesc(id);
            tryPrice = result.map(Bid::getTryPrice)
                    .orElse(0); //데이터가 없을 경우 tryPrice 0으로 설정
        }

        System.out.println("tryPrice: " + tryPrice);

        String formattedEndAt = formatRemainingTime(goods.getEndAt());

        return new GoodsResponse.GoodsDetailDTO(goods, tryPrice, formattedEndAt);

    }

    @Transactional
    public void updateGoodsStatus(GoodsRequest.GoodsStatusDTO goodsStatusDTO) {
        Goods goods = goodsRepository.findById(goodsStatusDTO.getGoodsId())
                .orElseThrow(() -> new Exception404("해당 물품은 존재하지 않습니다."));

        // 영속 상태에서 필드 값 변경
        goods.endAuction();
        // 별도의 save() 나 persist() 호출없이 자동으로 변경사항 반영

    }

    // 제품 등록하기
    @Transactional
    public void goodsSave(GoodsRequest.GoodsSaveDTO goodsSaveDTO) {
        goodsRepository.save(goodsSaveDTO.toEntity());
    }

    public List<GoodsResponse.GoodsDTO> getGoodsList(Integer categoryId, Integer page, Integer line) {
        List<Goods> goodList = goodsRepository.findByCategoryId(categoryId,page,line)
                .orElseThrow(() -> new Exception404("해당 카테고리의 물품이 존재하지 않습니다."));

        List<GoodsResponse.GoodsDTO> goodsList = new ArrayList<>();
        for (Goods goods : goodList) {
            bidRepository.findByGoodsDescIsNull(goods.getId()).ifPresentOrElse(bid -> goodsList.add(GoodsResponse.GoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(bid.getTryPrice())
                            .build()),
                    () -> goodsList.add(GoodsResponse.GoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(0)
                            .build()));
        }
        return goodsList;
    }

    // 제목 및 내용으로 물품 리스트 조회
    public List<GoodsResponse.GoodsDTO> searchGoodsList(GoodsRequest.SeacherGoodsDTO dto) {
        Optional<List<Goods>> searchGoodsList = goodsRepository.searchGoods(dto);
        List<GoodsResponse.GoodsDTO> goodsList = new ArrayList<>();

        for (Goods goods : searchGoodsList.get()) {
            bidRepository.findByGoodsDescIsNull(goods.getId()).ifPresentOrElse(bid -> goodsList.add(GoodsResponse.GoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(bid.getTryPrice())
                            .build()),
                    () -> goodsList.add(GoodsResponse.GoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(0)
                            .build()));
        }
        return goodsList;
    }
    // 내가 경매에 내놓은 물품 리스트 보기
    public List<GoodsResponse.UserGoodsDTO> mySellGoods(String username){
        User user = userRepository.findByUsername(username);
        List<Goods> bySellGoods = goodsRepository.findBySellGoods(user.getId());
       // PageUtil.offsetCount(page, 5), 5
        List<GoodsResponse.UserGoodsDTO> goodsList = new ArrayList<>();

        for (Goods goods : bySellGoods) {
            bidRepository.findByGoodsDescIsNull(goods.getId()).ifPresentOrElse(bid -> goodsList.add(GoodsResponse.UserGoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(bid.getTryPrice())
                            .build()),
                    () -> goodsList.add(GoodsResponse.UserGoodsDTO.builder()
                            .goods(goods)
                            .bidTryPrice(0)
                            .build()));
        }
        return goodsList;
    }
}
