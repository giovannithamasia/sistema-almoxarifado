package com.senai.sistema_almoxarifado.controller.produto;

import com.senai.sistema_almoxarifado.dto.produto.ProdutoAtualizarDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoDto;
import com.senai.sistema_almoxarifado.dto.produto.ProdutoRespostaDto;
import com.senai.sistema_almoxarifado.entity.ProdutoEntity;
import com.senai.sistema_almoxarifado.service.ProdutoService;
import com.senai.sistema_almoxarifado.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProdutoPageController {

    private final ProdutoService service;

    @GetMapping("/produtolista")
    public String getProdutoLista(@RequestParam(value = "termo", required = false) String termo, HttpSession session, Model model){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        List<ProdutoRespostaDto> listaProdutos;

        if (termo != null && !termo.trim().isEmpty()) {
            listaProdutos = service.buscarPersonalizada(termo);
        } else {
            listaProdutos = service.listarProdutos();
        }

        model.addAttribute("listaProdutos",listaProdutos);

        model.addAttribute("termo", termo);

        return "produtos/produtolista";
    }

    @GetMapping("/produtocadastrar")
    public String getCadastrarProduto(HttpSession session, Model model) {
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        model.addAttribute("produtoDto", new ProdutoDto(null, null, null, null, null));

        return "produtos/produtocadastrar";
    }

    @GetMapping("/produtoatualizar/{id}")
    public String getProdutoAtualizar(@PathVariable("id") Long id,
                                      Model model,
                                      HttpSession session){
        if (SessaoUtil.obterSessao(session) == null) {
            return "redirect:/login";
        }

        ProdutoEntity produto = service.buscarProdutoPorId(id);

        model.addAttribute("produtoAtualizacao",produto);

        return "produtos/produtoatualizar";
    }
}
