package ssoaks.ssoak.api.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssoaks.ssoak.api.auction.dto.response.ItemImageSimpleInfoDto;
import ssoaks.ssoak.api.auction.entity.Image;
import ssoaks.ssoak.api.auction.entity.Item;
import ssoaks.ssoak.api.auction.repository.ImageRepository;
import ssoaks.ssoak.api.auction.repository.ItemRepository;
import ssoaks.ssoak.api.auction.dto.response.ItemOverviewDto;
import ssoaks.ssoak.api.member.dto.response.ResMemberProfileDTO;
import ssoaks.ssoak.api.member.dto.response.ResOtherMemberProfileDTO;
import ssoaks.ssoak.api.member.entity.Member;
import ssoaks.ssoak.api.member.exception.NotAuthenticatedMemberException;
import ssoaks.ssoak.api.member.exception.NotFoundMemberException;
import ssoaks.ssoak.api.member.repository.MemberRepository;
import ssoaks.ssoak.common.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    private final ItemRepository itemRepository;

    private final ImageRepository imageRepository;


    @Override
    public Member getMemberByAuthentication() {
        long id = -1L;
        Optional<String> username = SecurityUtil.getCurrentUsername();
        if (username.isPresent()) {
            id = Long.parseLong(username.get());
        }
        return memberRepository.findById(id).orElse(null);
    }

    @Override
    public ResMemberProfileDTO getMyProfile() {

        Member member;
        Long memberSeq;

        try {
            member = getMemberByAuthentication();
            memberSeq = member.getSeq();

        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMyProfile() 회원 인증 실패");
        }

        ResMemberProfileDTO memberProfile = new ResMemberProfileDTO(member.getSeq(), member.getEmail(),
                member.getNickname(), member.getProfileImageUrl(), member.getGrade());

        return memberProfile;
    }

    @Override
    public List<ItemOverviewDto> getMySellingItems() {

        Long memberSeq;
        List<ItemOverviewDto> sellingItems;

        try {
            memberSeq = getMemberByAuthentication().getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMySellingItems() 회원 인증 실패");
        }

        try {
            sellingItems = itemRepository.getSellingItemOverviewsByMember(memberSeq);
            List<ItemImageSimpleInfoDto> sellingItemsImages = imageRepository.getSellingItemsImagesByMember(memberSeq);

            for (int i = 0; i < sellingItems.size(); i++) {
                sellingItems.get(i).setImageUrl(sellingItemsImages.get(i).getImageUrl());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("MemberServiceImpl getMySellingItems() 판매중 물품 조회 실패");
        }

        return sellingItems;
    }

    @Override
    public List<ItemOverviewDto> getMySoldItems() {
        Long memberSeq;
        List<ItemOverviewDto> soldItems;

        try {
            memberSeq = getMemberByAuthentication().getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMySoldItems() 회원정보 인증 실패");
        }

        try {
            soldItems = itemRepository.getSoldItemOverviewsByMember(memberSeq);
            List<ItemImageSimpleInfoDto> soldItemsImages = imageRepository.getSoldItemsImagesByMember(memberSeq);

            for (int i = 0; i < soldItems.size(); i++) {
                soldItems.get(i).setImageUrl(soldItemsImages.get(i).getImageUrl());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("MemberServiceImpl getMySoldItems() 판매완료 물품 조회 실패");
        }

        return soldItems;
    }

    @Override
    public List<ItemOverviewDto> getMyUnsoldItems() {

        Long memberSeq;
        List<ItemOverviewDto> unsoldItems;

        try {
            memberSeq = getMemberByAuthentication().getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMyUnsoldItems() 회원 인증 실패");
        }

        try {
            unsoldItems = itemRepository.getUnsoldItemOverviewsByMember(memberSeq);
            List<ItemImageSimpleInfoDto> unsoldItemsImages = imageRepository.getUnsoldItemsImagesByMember(memberSeq);

            for (int i = 0; i < unsoldItems.size(); i++) {
                unsoldItems.get(i).setImageUrl(unsoldItemsImages.get(i).getImageUrl());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("MemberServiceImpl getMyUnsoldItems() 판매완료 물품 조회 실패");
        }

        return unsoldItems;
    }

    @Transactional
    @Override
    public Integer deleteMember() {

        Member member;
        Long memberSeq;

        try {
            member = getMemberByAuthentication();
            memberSeq = member.getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl deleteMember() 회원 인증 실패");
        }

        try {
            member.deleteMember();
        } catch (Exception e) {
            return 409;
        }

        memberRepository.save(member);

        return 200;
    }

    @Override
    public ResOtherMemberProfileDTO getOtherMemberProfile(Long memberSeq) {

        Member member;
        Member otherMember;

        try {
            member = getMemberByAuthentication();
            member.getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getOtherMemberProfile() 회원 인증 실패");
        }

        otherMember = memberRepository.findBySeq(memberSeq).orElseThrow(() -> new NotFoundMemberException("회원을 찾을 수 없음"));

        // 나중에 count만 따로 가져오는 repository 메소드 만들어서 쓰는것이 나아보임.
        List<Item> soldItems = itemRepository.getSoldItemsByMember(memberSeq);

        ResOtherMemberProfileDTO memberProfile = new ResOtherMemberProfileDTO(otherMember.getSeq(), otherMember.getNickname(),
                otherMember.getProfileImageUrl(), otherMember.getGrade(), otherMember.getIsDeleted(), soldItems.size());

        return memberProfile;
    }

    @Override
    public List<ItemOverviewDto> getMyBoughtItems() {
        Long memberSeq;
        List<ItemOverviewDto> boughtItems;

        try {
            memberSeq = getMemberByAuthentication().getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMyBoughtItems() 회원 인증 실패");
        }

        try {
            boughtItems = itemRepository.getBoughtItemOverviewsByMember(memberSeq);
            List<ItemImageSimpleInfoDto> boughtItemsImages = imageRepository.getBoughtItemsImagesByMember(memberSeq);

            for (int i = 0; i < boughtItems.size(); i++) {
                boughtItems.get(i).setImageUrl(boughtItemsImages.get(i).getImageUrl());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("MemberServiceImpl getMyBoughtItems() 구매한 물품 조회 실패");
        }

        return boughtItems;
    }

    @Override
    public List<ItemOverviewDto> getMyLikedItems() {
        Long memberSeq;
        List<ItemOverviewDto> likedItems;

        try {
            memberSeq = getMemberByAuthentication().getSeq();
        } catch (Exception e) {
            throw new NotAuthenticatedMemberException("MemberServiceImpl getMyLikedItems() 회원 인증 실패");
        }

        System.out.println("1====================");
        try {
            likedItems = itemRepository.getLikedItemOverviewsByMember(memberSeq);
            System.out.println("2====================");
            List<ItemImageSimpleInfoDto> likedItemsImages = imageRepository.getLikedItemsImagesByMember(memberSeq);
            System.out.println("3====================");

            for (int i = 0; i < likedItems.size(); i++) {
                likedItems.get(i).setImageUrl(likedItemsImages.get(i).getImageUrl());
            }
            System.out.println("4====================");
        } catch (Exception e) {
            throw new IllegalArgumentException("MemberServiceImpl getMyLikedItems() 찜한 물품 조회 실패");
        }

        return likedItems;
    }
}

