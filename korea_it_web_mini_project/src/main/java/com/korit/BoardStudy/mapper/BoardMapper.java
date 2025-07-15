package com.korit.BoardStudy.mapper;

import com.korit.BoardStudy.entity.Board;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BoardMapper {

    //게시물 추가
    int addBoard(Board board);

    //게시물 단건 조회
    Optional<Board> getBoardByBoardId(Integer boardId);

    //게시물 다건 조회
    List<Board> getBoardList();
}
