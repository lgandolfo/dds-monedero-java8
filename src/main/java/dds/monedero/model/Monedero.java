package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Monedero {

  private double saldo = 0;
  private double maximoExtraccionDiaria = 1000;
  private List<Movimiento> movimientos;

  public Monedero() {
    saldo = 0;
    movimientos = new ArrayList<>();
  }

  public Monedero(double montoInicial) {
    saldo = montoInicial;
    movimientos = new ArrayList<>();
  }

  public void poner(double cuanto) {

    montoEsValido(cuanto);

    puedeDepositar();

    Deposito nuevoDeposito = new Deposito(LocalDate.now(), cuanto);
    agregarMovimiento(nuevoDeposito);
    setSaldo(nuevoDeposito.realizarSobre(saldo));
  }

  public void sacar(double cuanto) {

    montoEsValido(cuanto);

    tieneSaldo(cuanto);

    tieneLimiteDiario(cuanto);

    Extraccion nuevaExtraccion = new Extraccion(LocalDate.now(), cuanto);
    agregarMovimiento(nuevaExtraccion);
    setSaldo(nuevaExtraccion.realizarSobre(saldo));

  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.getClass().equals(Deposito.class) && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private void montoEsValido(double monto){
    if(monto <= 0){
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  public void puedeDepositar(){
    int depositosDeHoy = cantidadDepositosHoy();

    if (depositosDeHoy >= 3){
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

  }

  public int cantidadDepositosHoy(){
    List<Movimiento> depositos = movimientos.stream().filter(movimiento -> movimiento.getClass().equals(Deposito.class)).collect(Collectors.toList());
    return (int) depositos.stream().filter(deposito -> deposito.esDeLaFecha(LocalDate.now())).count();
  }

  private double obtenerMontoExtraibleActual(){
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    return maximoExtraccionDiaria - montoExtraidoHoy;
  }

  private void tieneSaldo(double monto){
    if(getSaldo() < monto){
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  private void tieneLimiteDiario(double monto){
    double limite = obtenerMontoExtraibleActual();
    if (monto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
              + " diarios, límite: " + limite);
    }
  }

  private void agregarMovimiento(Movimiento movimiento){
    movimientos.add(movimiento);
  }

}
